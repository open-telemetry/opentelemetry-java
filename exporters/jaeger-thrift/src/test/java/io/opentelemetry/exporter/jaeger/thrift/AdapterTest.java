/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import io.jaegertracing.thriftjava.Log;
import io.jaegertracing.thriftjava.SpanRef;
import io.jaegertracing.thriftjava.SpanRefType;
import io.jaegertracing.thriftjava.Tag;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Adapter}. */
class AdapterTest {
  private static final BaseEncoding hex = BaseEncoding.base16().lowerCase();
  private static final String LINK_TRACE_ID = "ff000000000000000000000000cba123";
  private static final String LINK_SPAN_ID = "0000000000fed456";
  private static final String TRACE_ID = "0000000000000000ff00000000abc123";
  private static final String SPAN_ID = "ff00000000def456";
  private static final String PARENT_SPAN_ID = "0000000000aef789";

  @Test
  void testThriftSpans() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs, SpanKind.SERVER);
    List<SpanData> spans = Collections.singletonList(span);

    List<io.jaegertracing.thriftjava.Span> jaegerSpans = Adapter.toJaeger(spans);

    // the span contents are checked somewhere else
    assertThat(jaegerSpans).hasSize(1);
  }

  @Test
  void testThriftSpan() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs, SpanKind.SERVER, 2, 4);

    // test
    io.jaegertracing.thriftjava.Span jaegerSpan = Adapter.toJaeger(span);

    String rebuildTraceId =
        traceIdFromLongs(jaegerSpan.getTraceIdHigh(), jaegerSpan.getTraceIdLow());
    assertThat(rebuildTraceId).isEqualTo(span.getTraceId());
    assertThat(spanIdFromLong(jaegerSpan.getSpanId())).isEqualTo(span.getSpanId());
    assertThat(jaegerSpan.getOperationName()).isEqualTo("GET /api/endpoint");
    assertThat(jaegerSpan.getStartTime()).isEqualTo(MILLISECONDS.toMicros(startMs));
    assertThat(jaegerSpan.getDuration()).isEqualTo(MILLISECONDS.toMicros(duration));

    assertThat(jaegerSpan.getTagsSize()).isEqualTo(7);
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_KIND).getVStr()).isEqualTo("server");
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_STATUS_CODE).getVLong())
        .isEqualTo(0);
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_STATUS_MESSAGE).getVStr())
        .isEqualTo("ok!");
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_DROPPED_EVENTS_COUNT).getVLong())
        .isEqualTo(1);
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_DROPPED_ATTRIBUTES_COUNT).getVLong())
        .isEqualTo(3);

    assertThat(jaegerSpan.getLogsSize()).isEqualTo(1);
    Log log = jaegerSpan.getLogs().get(0);
    assertThat(getValue(log.getFields(), Adapter.KEY_LOG_EVENT).getVStr())
        .isEqualTo("the log message");
    assertThat(getValue(log.getFields(), "foo").getVStr()).isEqualTo("bar");

    assertThat(jaegerSpan.getReferencesSize()).isEqualTo(2);

    assertHasFollowsFrom(jaegerSpan);
    assertHasParent(jaegerSpan);
  }

  @Test
  void testThriftSpan_internal() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs, SpanKind.INTERNAL);

    // test
    io.jaegertracing.thriftjava.Span jaegerSpan = Adapter.toJaeger(span);

    assertThat(jaegerSpan.getTagsSize()).isEqualTo(4);
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_KIND)).isNull();
  }

  @Test
  void testJaegerLogs() {
    // prepare
    EventData eventsData = getTimedEvent();

    // test
    Collection<Log> logs = Adapter.toJaegerLogs(Collections.singletonList(eventsData));

    // verify
    assertThat(logs).hasSize(1);
  }

  @Test
  void testJaegerLog() {
    // prepare
    EventData event = getTimedEvent();

    // test
    Log log = Adapter.toJaegerLog(event);

    // verify
    assertThat(log.getFieldsSize()).isEqualTo(2);

    assertThat(getValue(log.getFields(), Adapter.KEY_LOG_EVENT).getVStr())
        .isEqualTo("the log message");
    assertThat(getValue(log.getFields(), "foo").getVStr()).isEqualTo("bar");
    assertThat(getValue(log.getFields(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT)).isNull();
  }

  @Test
  void jaegerLog_droppedAttributes() {
    EventData event = getTimedEvent(3);

    // test
    Log log = Adapter.toJaegerLog(event);

    // verify
    assertThat(getValue(log.getFields(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT).getVLong())
        .isEqualTo(2);
  }

  @Test
  void testKeyValue() {
    // test
    Tag kvB = Adapter.toTag(booleanKey("valueB"), true);
    Tag kvD = Adapter.toTag(doubleKey("valueD"), 1.);
    Tag kvI = Adapter.toTag(longKey("valueI"), 2L);
    Tag kvS = Adapter.toTag(stringKey("valueS"), "foobar");
    Tag kvArrayB = Adapter.toTag(booleanArrayKey("valueArrayB"), Arrays.asList(true, false));
    Tag kvArrayD = Adapter.toTag(doubleArrayKey("valueArrayD"), Arrays.asList(1.2345, 6.789));
    Tag kvArrayI = Adapter.toTag(longArrayKey("valueArrayI"), Arrays.asList(12345L, 67890L));
    Tag kvArrayS = Adapter.toTag(stringArrayKey("valueArrayS"), Arrays.asList("foobar", "barfoo"));

    // verify
    assertThat(kvB.isVBool()).isTrue();

    assertThat(kvD.getVDouble()).isEqualTo(1);
    assertThat(kvI.getVLong()).isEqualTo(2);
    assertThat(kvS.getVStr()).isEqualTo("foobar");
    assertThat(kvArrayB.getVStr()).isEqualTo("[true,false]");
    assertThat(kvArrayD.getVStr()).isEqualTo("[1.2345,6.789]");
    assertThat(kvArrayI.getVStr()).isEqualTo("[12345,67890]");
    assertThat(kvArrayS.getVStr()).isEqualTo("[\"foobar\",\"barfoo\"]");
  }

  @Test
  void testSpanRefs() {
    // prepare
    LinkData link =
        LinkData.create(createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    Collection<SpanRef> spanRefs = Adapter.toSpanRefs(Collections.singletonList(link));

    // verify
    assertThat(spanRefs).hasSize(1); // the actual span ref is tested in another test
  }

  @Test
  void testSpanRef() {
    // prepare
    LinkData link = LinkData.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    SpanRef spanRef = Adapter.toSpanRef(link);

    // verify
    assertThat(spanIdFromLong(spanRef.getSpanId())).isEqualTo(SPAN_ID);
    assertThat(traceIdFromLongs(spanRef.getTraceIdHigh(), spanRef.getTraceIdLow()))
        .isEqualTo(TRACE_ID);
    assertThat(spanRef.getRefType()).isEqualTo(SpanRefType.FOLLOWS_FROM);
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
            .setStartEpochNanos(MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(MILLISECONDS.toNanos(endMs))
            .setKind(SpanKind.SERVER)
            .setStatus(StatusData.error())
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
            .setSpanContext(createSpanContext(TRACE_ID, SPAN_ID))
            .setName("GET /api/endpoint")
            .setStartEpochNanos(MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(MILLISECONDS.toNanos(endMs))
            .setKind(SpanKind.SERVER)
            .setStatus(StatusData.error())
            .setAttributes(attributes)
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    io.jaegertracing.thriftjava.Span jaegerSpan = Adapter.toJaeger(span);
    assertThat(getValue(jaegerSpan.getTags(), "error.type").getVStr())
        .isEqualTo(this.getClass().getName());
    assertThat(getValue(jaegerSpan.getTags(), "error").isVBool()).isTrue();
  }

  private static EventData getTimedEvent() {
    return getTimedEvent(-1);
  }

  private static EventData getTimedEvent(int totalAttributeCount) {
    long epochNanos = MILLISECONDS.toNanos(System.currentTimeMillis());
    Attributes attributes = Attributes.of(stringKey("foo"), "bar");
    if (totalAttributeCount <= 0) {
      totalAttributeCount = attributes.size();
    }
    return EventData.create(epochNanos, "the log message", attributes, totalAttributeCount);
  }

  private static SpanData getSpanData(long startMs, long endMs, SpanKind kind) {
    return getSpanData(startMs, endMs, kind, 1, 1);
  }

  private static SpanData getSpanData(
      long startMs, long endMs, SpanKind kind, int totalRecordedEvents, int totalAttributeCount) {
    Attributes attributes = Attributes.of(booleanKey("valueB"), true);

    LinkData link = LinkData.create(createSpanContext(LINK_TRACE_ID, LINK_SPAN_ID), attributes);

    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(createSpanContext(TRACE_ID, SPAN_ID))
        .setParentSpanContext(
            SpanContext.create(
                TRACE_ID, PARENT_SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(MILLISECONDS.toNanos(startMs))
        .setEndEpochNanos(MILLISECONDS.toNanos(endMs))
        .setAttributes(Attributes.of(booleanKey("valueB"), true))
        .setTotalAttributeCount(totalAttributeCount)
        .setEvents(Collections.singletonList(getTimedEvent()))
        .setTotalRecordedEvents(totalRecordedEvents)
        .setLinks(Collections.singletonList(link))
        .setTotalRecordedLinks(1)
        .setKind(kind)
        .setResource(Resource.create(Attributes.empty()))
        .setStatus(StatusData.create(StatusCode.OK, "ok!"))
        .build();
  }

  private static SpanContext createSpanContext(String traceId, String spanId) {
    return SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
  }

  @Nullable
  private static Tag getValue(List<Tag> tagsList, String s) {
    for (Tag kv : tagsList) {
      if (kv.getKey().equals(s)) {
        return kv;
      }
    }
    return null;
  }

  private static void assertHasFollowsFrom(io.jaegertracing.thriftjava.Span jaegerSpan) {
    boolean found = false;
    for (SpanRef spanRef : jaegerSpan.getReferences()) {

      if (SpanRefType.FOLLOWS_FROM.equals(spanRef.getRefType())) {
        assertThat(traceIdFromLongs(spanRef.getTraceIdHigh(), spanRef.getTraceIdLow()))
            .isEqualTo(LINK_TRACE_ID);
        assertThat(spanIdFromLong(spanRef.getSpanId())).isEqualTo(LINK_SPAN_ID);
        found = true;
      }
    }
    assertThat(found).isTrue();
  }

  private static void assertHasParent(io.jaegertracing.thriftjava.Span jaegerSpan) {
    boolean found = false;
    for (SpanRef spanRef : jaegerSpan.getReferences()) {
      if (SpanRefType.CHILD_OF.equals(spanRef.getRefType())) {
        assertThat(traceIdFromLongs(spanRef.getTraceIdHigh(), spanRef.getTraceIdLow()))
            .isEqualTo(TRACE_ID);
        assertThat(spanIdFromLong(spanRef.getSpanId())).isEqualTo(PARENT_SPAN_ID);
        found = true;
      }
    }
    assertThat(found).isTrue();
    assertThat(spanIdFromLong(jaegerSpan.getParentSpanId())).isEqualTo(PARENT_SPAN_ID);
  }

  private static String traceIdFromLongs(long high, long low) {
    return hex.encode(
        ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN).putLong(high).putLong(low).array());
  }

  private static String spanIdFromLong(long id) {
    return hex.encode(ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(id).array());
  }
}
