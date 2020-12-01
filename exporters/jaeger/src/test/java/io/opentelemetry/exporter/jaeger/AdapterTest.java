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

import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.extension.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Adapter}. */
class AdapterTest {

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

    SpanData span = getSpanData(startMs, endMs);
    List<SpanData> spans = Collections.singletonList(span);

    Collection<Model.Span> jaegerSpans = Adapter.toJaeger(spans);

    // the span contents are checked somewhere else
    assertThat(jaegerSpans).hasSize(1);
  }

  @Test
  void testProtoSpan() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs);

    // test
    Model.Span jaegerSpan = Adapter.toJaeger(span);
    assertThat(jaegerSpan.getTraceId())
        .isEqualTo(TraceProtoUtils.toProtoTraceId(span.getTraceId()));
    assertThat(jaegerSpan.getSpanId()).isEqualTo(TraceProtoUtils.toProtoSpanId(span.getSpanId()));
    assertThat(jaegerSpan.getOperationName()).isEqualTo("GET /api/endpoint");
    assertThat(jaegerSpan.getStartTime()).isEqualTo(Timestamps.fromMillis(startMs));
    assertThat(Durations.toMillis(jaegerSpan.getDuration())).isEqualTo(duration);

    assertThat(jaegerSpan.getTagsCount()).isEqualTo(4);
    Model.KeyValue keyValue = getValue(jaegerSpan.getTagsList(), Adapter.KEY_SPAN_KIND);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("server");

    assertThat(jaegerSpan.getLogsCount()).isEqualTo(1);
    Model.Log log = jaegerSpan.getLogs(0);
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_LOG_EVENT);
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
  void testJaegerLogs() {
    // prepare
    Event eventsData = getTimedEvent();

    // test
    Collection<Model.Log> logs = Adapter.toJaegerLogs(Collections.singletonList(eventsData));

    // verify
    assertThat(logs).hasSize(1);
  }

  @Test
  void testJaegerLog() {
    // prepare
    Event event = getTimedEvent();

    // test
    Model.Log log = Adapter.toJaegerLog(event);

    // verify
    assertThat(log.getFieldsCount()).isEqualTo(2);

    Model.KeyValue keyValue = getValue(log.getFieldsList(), Adapter.KEY_LOG_EVENT);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("the log message");
    keyValue = getValue(log.getFieldsList(), "foo");
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVStr()).isEqualTo("bar");
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT);
    assertThat(keyValue).isNull();

    // verify dropped_attributes_count
    event = getTimedEvent(3);
    log = Adapter.toJaegerLog(event);
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT);
    assertThat(keyValue).isNotNull();
    assertThat(keyValue.getVInt64()).isEqualTo(2);
  }

  @Test
  void testKeyValue() {
    // test
    Model.KeyValue kvB = Adapter.toKeyValue(booleanKey("valueB"), true);
    Model.KeyValue kvD = Adapter.toKeyValue(doubleKey("valueD"), 1.);
    Model.KeyValue kvI = Adapter.toKeyValue(longKey("valueI"), 2L);
    Model.KeyValue kvS = Adapter.toKeyValue(stringKey("valueS"), "foobar");
    Model.KeyValue kvArrayB =
        Adapter.toKeyValue(booleanArrayKey("valueArrayB"), Arrays.asList(true, false));
    Model.KeyValue kvArrayD =
        Adapter.toKeyValue(doubleArrayKey("valueArrayD"), Arrays.asList(1.2345, 6.789));
    Model.KeyValue kvArrayI =
        Adapter.toKeyValue(longArrayKey("valueArrayI"), Arrays.asList(12345L, 67890L));
    Model.KeyValue kvArrayS =
        Adapter.toKeyValue(stringArrayKey("valueArrayS"), Arrays.asList("foobar", "barfoo"));

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
    Link link =
        Link.create(createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    Collection<Model.SpanRef> spanRefs = Adapter.toSpanRefs(Collections.singletonList(link));

    // verify
    assertThat(spanRefs).hasSize(1); // the actual span ref is tested in another test
  }

  @Test
  void testSpanRef() {
    // prepare
    Link link = Link.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    Model.SpanRef spanRef = Adapter.toSpanRef(link);

    // verify
    assertThat(spanRef.getSpanId()).isEqualTo(TraceProtoUtils.toProtoSpanId(SPAN_ID));
    assertThat(spanRef.getTraceId()).isEqualTo(TraceProtoUtils.toProtoTraceId(TRACE_ID));
    assertThat(spanRef.getRefType()).isEqualTo(Model.SpanRefType.FOLLOWS_FROM);
  }

  @Test
  void testStatusNotUnset() {
    long startMs = System.currentTimeMillis();
    long endMs = startMs + 900;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setKind(Span.Kind.SERVER)
            .setStatus(Status.error())
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    assertThat(Adapter.toJaeger(span)).isNotNull();
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
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setKind(Span.Kind.SERVER)
            .setStatus(Status.error())
            .setAttributes(attributes)
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    Model.Span jaegerSpan = Adapter.toJaeger(span);
    Model.KeyValue errorType = getValue(jaegerSpan.getTagsList(), "error.type");
    assertThat(errorType).isNotNull();
    assertThat(errorType.getVStr()).isEqualTo(this.getClass().getName());
    Model.KeyValue error = getValue(jaegerSpan.getTagsList(), "error");
    assertThat(error).isNotNull();
    assertThat(error.getVBool()).isTrue();
  }

  private static Event getTimedEvent() {
    return getTimedEvent(-1);
  }

  private static Event getTimedEvent(int totalAttributeCount) {
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    Attributes attributes = Attributes.of(stringKey("foo"), "bar");
    if (totalAttributeCount <= 0) {
      totalAttributeCount = attributes.size();
    }
    return Event.create(epochNanos, "the log message", attributes, totalAttributeCount);
  }

  private static SpanData getSpanData(long startMs, long endMs) {
    Attributes attributes = Attributes.of(booleanKey("valueB"), true);

    Link link = Link.create(createSpanContext(LINK_TRACE_ID, LINK_SPAN_ID), attributes);

    return TestSpanData.builder()
        .setHasEnded(true)
        .setTraceId(TRACE_ID)
        .setSpanId(SPAN_ID)
        .setParentSpanContext(
            SpanContext.create(
                TRACE_ID, PARENT_SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
        .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
        .setAttributes(Attributes.of(booleanKey("valueB"), true))
        .setEvents(Collections.singletonList(getTimedEvent()))
        .setTotalRecordedEvents(1)
        .setLinks(Collections.singletonList(link))
        .setTotalRecordedLinks(1)
        .setKind(Span.Kind.SERVER)
        .setResource(Resource.create(Attributes.empty()))
        .setStatus(Status.ok())
        .build();
  }

  private static SpanContext createSpanContext(String traceId, String spanId) {
    return SpanContext.create(
        traceId, spanId, TraceFlags.getDefault(), TraceState.builder().build());
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
        assertThat(spanRef.getTraceId()).isEqualTo(TraceProtoUtils.toProtoTraceId(LINK_TRACE_ID));
        assertThat(spanRef.getSpanId()).isEqualTo(TraceProtoUtils.toProtoSpanId(LINK_SPAN_ID));
        found = true;
      }
    }
    assertThat(found).withFailMessage("Should have found the follows-from reference").isTrue();
  }

  private static void assertHasParent(Model.Span jaegerSpan) {
    boolean found = false;
    for (Model.SpanRef spanRef : jaegerSpan.getReferencesList()) {
      if (Model.SpanRefType.CHILD_OF.equals(spanRef.getRefType())) {
        assertThat(spanRef.getTraceId()).isEqualTo(TraceProtoUtils.toProtoTraceId(TRACE_ID));
        assertThat(spanRef.getSpanId()).isEqualTo(TraceProtoUtils.toProtoSpanId(PARENT_SPAN_ID));
        found = true;
      }
    }
    assertThat(found).withFailMessage("Should have found the parent reference").isTrue();
  }
}
