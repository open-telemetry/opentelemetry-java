/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class PostSpansRequestMarshalerTest {

  private static final String KEY_LOG_EVENT = "event";
  private static final String KEY_EVENT_DROPPED_ATTRIBUTES_COUNT =
      "otel.event.dropped_attributes_count";
  private static final String KEY_DROPPED_ATTRIBUTES_COUNT = "otel.dropped_attributes_count";
  private static final String KEY_DROPPED_EVENTS_COUNT = "otel.dropped_events_count";
  private static final String KEY_SPAN_KIND = "span.kind";

  private static final String LINK_TRACE_ID = "00000000000000000000000000cba123";
  private static final String LINK_SPAN_ID = "0000000000fed456";
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";
  private static final String PARENT_SPAN_ID = "0000000000aef789";

  @Test
  void testProtoSpans() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs, SpanKind.SERVER);
    List<SpanData> spans = Collections.singletonList(span);

    SpanMarshaler[] jaegerSpans = SpanMarshaler.createRepeated(spans);

    // the span contents are checked somewhere else
    assertThat(jaegerSpans).hasSize(1);
  }

  @Test
  @SuppressWarnings({"ProtoTimestampGetSecondsGetNano", "ProtoDurationGetSecondsGetNano"})
  void testProtoSpan() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs, SpanKind.SERVER, 2);

    // test
    Model.Span jaegerSpan = parse(Model.Span.getDefaultInstance(), SpanMarshaler.create(span));
    assertThat(TraceId.fromBytes(jaegerSpan.getTraceId().toByteArray()))
        .isEqualTo(span.getTraceId());
    assertThat(SpanId.fromBytes(jaegerSpan.getSpanId().toByteArray())).isEqualTo(span.getSpanId());
    assertThat(jaegerSpan.getOperationName()).isEqualTo("GET /api/endpoint");
    assertThat(jaegerSpan.getStartTime()).isEqualTo(Timestamps.fromMillis(startMs));
    assertThat(jaegerSpan.getDuration()).isEqualTo(Durations.fromMillis(duration));

    assertThat(jaegerSpan.getTagsCount()).isEqualTo(6);
    Model.KeyValue keyValue = getValue(jaegerSpan.getTagsList(), KEY_SPAN_KIND);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("server");

    Model.KeyValue droppedAttributes =
        getValue(jaegerSpan.getTagsList(), KEY_DROPPED_ATTRIBUTES_COUNT);
    assertThat(droppedAttributes)
        .isEqualTo(
            Model.KeyValue.newBuilder()
                .setKey(KEY_DROPPED_ATTRIBUTES_COUNT)
                .setVType(Model.ValueType.INT64)
                .setVInt64(2)
                .build());

    assertThat(jaegerSpan.getLogsCount()).isEqualTo(1);
    Model.KeyValue droppedEvents = getValue(jaegerSpan.getTagsList(), KEY_DROPPED_EVENTS_COUNT);
    assertThat(droppedEvents)
        .isEqualTo(
            Model.KeyValue.newBuilder()
                .setKey(KEY_DROPPED_EVENTS_COUNT)
                .setVType(Model.ValueType.INT64)
                .setVInt64(1)
                .build());

    Model.Log log = jaegerSpan.getLogs(0);
    keyValue = getValue(log.getFieldsList(), KEY_LOG_EVENT);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("the log message");
    keyValue = getValue(log.getFieldsList(), "foo");
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("bar");

    assertThat(jaegerSpan.getReferencesCount()).isEqualTo(2);

    assertHasFollowsFrom(jaegerSpan);
    assertHasParent(jaegerSpan);
  }

  @Test
  void testProtoSpan_internal() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs, SpanKind.INTERNAL);

    // test
    Model.Span jaegerSpan = parse(Model.Span.getDefaultInstance(), SpanMarshaler.create(span));
    Model.KeyValue keyValue = getValue(jaegerSpan.getTagsList(), KEY_SPAN_KIND);
    assertThat(keyValue).isNull();
  }

  @Test
  void testJaegerLogs() {
    // prepare
    EventData eventsData = getTimedEvent();

    // test
    LogMarshaler[] logs = LogMarshaler.createRepeated(Collections.singletonList(eventsData));

    // verify
    assertThat(logs).hasSize(1);
  }

  @Test
  void testJaegerLog() {
    // prepare
    EventData event = getTimedEvent();

    // test
    Model.Log log = parse(Model.Log.getDefaultInstance(), LogMarshaler.create(event));

    // verify
    assertThat(log.getFieldsCount()).isEqualTo(2);

    Model.KeyValue keyValue = getValue(log.getFieldsList(), KEY_LOG_EVENT);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("the log message");
    keyValue = getValue(log.getFieldsList(), "foo");
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("bar");
    keyValue = getValue(log.getFieldsList(), KEY_EVENT_DROPPED_ATTRIBUTES_COUNT);
    assertThat(keyValue).isNull();

    // verify dropped_attributes_count
    event = getTimedEvent(3);
    log = parse(Model.Log.getDefaultInstance(), LogMarshaler.create(event));
    keyValue = getValue(log.getFieldsList(), KEY_EVENT_DROPPED_ATTRIBUTES_COUNT);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVInt64()).isEqualTo(2);
  }

  @Test
  void testKeyValue() {
    // test
    Model.KeyValue kvB =
        parse(
            Model.KeyValue.getDefaultInstance(),
            KeyValueMarshaler.create(booleanKey("valueB"), true));
    Model.KeyValue kvD =
        parse(
            Model.KeyValue.getDefaultInstance(), KeyValueMarshaler.create(doubleKey("valueD"), 1.));
    Model.KeyValue kvI =
        parse(Model.KeyValue.getDefaultInstance(), KeyValueMarshaler.create(longKey("valueI"), 2L));
    Model.KeyValue kvS =
        parse(
            Model.KeyValue.getDefaultInstance(),
            KeyValueMarshaler.create(stringKey("valueS"), "foobar"));
    Model.KeyValue kvArrayB =
        parse(
            Model.KeyValue.getDefaultInstance(),
            KeyValueMarshaler.create(booleanArrayKey("valueArrayB"), Arrays.asList(true, false)));
    Model.KeyValue kvArrayD =
        parse(
            Model.KeyValue.getDefaultInstance(),
            KeyValueMarshaler.create(doubleArrayKey("valueArrayD"), Arrays.asList(1.2345, 6.789)));
    Model.KeyValue kvArrayI =
        parse(
            Model.KeyValue.getDefaultInstance(),
            KeyValueMarshaler.create(longArrayKey("valueArrayI"), Arrays.asList(12345L, 67890L)));
    Model.KeyValue kvArrayS =
        parse(
            Model.KeyValue.getDefaultInstance(),
            KeyValueMarshaler.create(
                stringArrayKey("valueArrayS"), Arrays.asList("foobar", "barfoo")));

    // verify
    assertThat(kvB.getVBool()).isTrue();
    assertThat(kvB.getVType()).isEqualTo(Model.ValueType.BOOL);
    assertThat(kvD.getVFloat64()).isEqualTo(1.);
    assertThat(kvD.getVType()).isEqualTo(Model.ValueType.FLOAT64);
    assertThat(kvI.getVInt64()).isEqualTo(2);
    assertThat(kvI.getVType()).isEqualTo(Model.ValueType.INT64);
    assertThat(kvS.getVStr()).isEqualTo("foobar");
    assertThat(kvS.getVStrBytes().toStringUtf8()).isEqualTo("foobar");
    assertThat(kvS.getVType()).isEqualTo(Model.ValueType.STRING);
    assertThat(kvArrayB.getVStr()).isEqualTo("[true,false]");
    assertThat(kvArrayB.getVStrBytes().toStringUtf8()).isEqualTo("[true,false]");
    assertThat(kvArrayB.getVType()).isEqualTo(Model.ValueType.STRING);
    assertThat(kvArrayD.getVStr()).isEqualTo("[1.2345,6.789]");
    assertThat(kvArrayD.getVStrBytes().toStringUtf8()).isEqualTo("[1.2345,6.789]");
    assertThat(kvArrayD.getVType()).isEqualTo(Model.ValueType.STRING);
    assertThat(kvArrayI.getVStr()).isEqualTo("[12345,67890]");
    assertThat(kvArrayI.getVStrBytes().toStringUtf8()).isEqualTo("[12345,67890]");
    assertThat(kvArrayI.getVType()).isEqualTo(Model.ValueType.STRING);
    assertThat(kvArrayS.getVStr()).isEqualTo("[\"foobar\",\"barfoo\"]");
    assertThat(kvArrayS.getVStrBytes().toStringUtf8()).isEqualTo("[\"foobar\",\"barfoo\"]");
    assertThat(kvArrayS.getVType()).isEqualTo(Model.ValueType.STRING);
  }

  @Test
  void testSpanRefs() {
    // prepare
    LinkData link =
        LinkData.create(createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    List<SpanRefMarshaler> spanRefs =
        SpanRefMarshaler.createRepeated(Collections.singletonList(link));

    // verify
    assertThat(spanRefs).hasSize(1); // the actual span ref is tested in another test
  }

  @Test
  void testSpanRef() {
    // prepare
    LinkData link = LinkData.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    Model.SpanRef spanRef =
        parse(Model.SpanRef.getDefaultInstance(), SpanRefMarshaler.create(link));

    // verify
    assertThat(SpanId.fromBytes(spanRef.getSpanId().toByteArray())).isEqualTo(SPAN_ID);
    assertThat(TraceId.fromBytes(spanRef.getTraceId().toByteArray())).isEqualTo(TRACE_ID);
    assertThat(spanRef.getRefType()).isEqualTo(Model.SpanRefType.FOLLOWS_FROM);
  }

  @Test
  void testStatusNotUnset() {
    long startMs = System.currentTimeMillis();
    long endMs = startMs + 900;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(createSpanContext(TRACE_ID, SPAN_ID))
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setKind(SpanKind.SERVER)
            .setStatus(StatusData.error())
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    assertThat(SpanMarshaler.create(span)).isNotNull();
  }

  @Test
  void testSpanError() {
    Attributes attributes =
        Attributes.of(
            stringKey("error.type"),
            this.getClass().getName(),
            stringKey("error.message"),
            "server error");
    long startMs = System.currentTimeMillis();
    long endMs = startMs + 900;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(createSpanContext(TRACE_ID, SPAN_ID))
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setKind(SpanKind.SERVER)
            .setStatus(StatusData.error())
            .setAttributes(attributes)
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    Model.Span jaegerSpan = parse(Model.Span.getDefaultInstance(), SpanMarshaler.create(span));
    Model.KeyValue errorType = getValue(jaegerSpan.getTagsList(), "error.type");
    assertThat(errorType).isNotNull();
    assertThat(errorType.getVStr()).isEqualTo(this.getClass().getName());
    Model.KeyValue error = getValue(jaegerSpan.getTagsList(), "error");
    assertThat(error).isNotNull();
    assertThat(error.getVBool()).isTrue();
  }

  private static EventData getTimedEvent() {
    return getTimedEvent(-1);
  }

  private static EventData getTimedEvent(int totalAttributeCount) {
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    Attributes attributes = Attributes.of(stringKey("foo"), "bar");
    if (totalAttributeCount <= 0) {
      totalAttributeCount = attributes.size();
    }
    return EventData.create(epochNanos, "the log message", attributes, totalAttributeCount);
  }

  private static SpanData getSpanData(long startMs, long endMs, SpanKind kind) {
    return getSpanData(startMs, endMs, kind, 1);
  }

  private static SpanData getSpanData(
      long startMs, long endMs, SpanKind kind, int totalRecordedEvents) {
    Attributes attributes = Attributes.of(booleanKey("valueB"), true);

    LinkData link = LinkData.create(createSpanContext(LINK_TRACE_ID, LINK_SPAN_ID), attributes);

    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(createSpanContext(TRACE_ID, SPAN_ID))
        .setParentSpanContext(
            SpanContext.create(
                TRACE_ID, PARENT_SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
        .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
        .setAttributes(Attributes.of(booleanKey("valueB"), true))
        .setTotalAttributeCount(3)
        .setEvents(Collections.singletonList(getTimedEvent()))
        .setTotalRecordedEvents(totalRecordedEvents)
        .setLinks(Collections.singletonList(link))
        .setTotalRecordedLinks(1)
        .setKind(kind)
        .setResource(Resource.create(Attributes.empty()))
        .setStatus(StatusData.ok())
        .build();
  }

  private static SpanContext createSpanContext(String traceId, String spanId) {
    return SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
  }

  @Nullable
  private static Model.KeyValue getValue(List<Model.KeyValue> tagsList, String s) {
    for (Model.KeyValue kv : tagsList) {
      if (kv.getKey().equals(s)) {
        return kv;
      }
    }
    return null;
  }

  private static void assertHasFollowsFrom(Model.Span jaegerSpan) {
    boolean found = false;
    for (Model.SpanRef spanRef : jaegerSpan.getReferencesList()) {
      if (Model.SpanRefType.FOLLOWS_FROM.equals(spanRef.getRefType())) {
        assertThat(TraceId.fromBytes(spanRef.getTraceId().toByteArray())).isEqualTo(LINK_TRACE_ID);
        assertThat(SpanId.fromBytes(spanRef.getSpanId().toByteArray())).isEqualTo(LINK_SPAN_ID);
        found = true;
      }
    }
    assertThat(found).withFailMessage("Should have found the follows-from reference").isTrue();
  }

  private static void assertHasParent(Model.Span jaegerSpan) {
    boolean found = false;
    for (Model.SpanRef spanRef : jaegerSpan.getReferencesList()) {
      if (Model.SpanRefType.CHILD_OF.equals(spanRef.getRefType())) {
        assertThat(TraceId.fromBytes(spanRef.getTraceId().toByteArray())).isEqualTo(TRACE_ID);
        assertThat(SpanId.fromBytes(spanRef.getSpanId().toByteArray())).isEqualTo(PARENT_SPAN_ID);
        found = true;
      }
    }
    assertThat(found).withFailMessage("Should have found the parent reference").isTrue();
  }

  @SuppressWarnings("unchecked")
  private static <T extends Message> T parse(T prototype, Marshaler marshaler) {
    byte[] serialized = toByteArray(marshaler);
    T result;
    try {
      result = (T) prototype.newBuilderForType().mergeFrom(serialized).build();
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);
    return result;
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeBinaryTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }
}
