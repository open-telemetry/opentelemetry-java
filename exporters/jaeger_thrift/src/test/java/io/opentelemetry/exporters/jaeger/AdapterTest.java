/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import static io.opentelemetry.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.common.AttributeKey.booleanKey;
import static io.opentelemetry.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.common.AttributeKey.doubleKey;
import static io.opentelemetry.common.AttributeKey.longArrayKey;
import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.jaegertracing.thriftjava.Log;
import io.jaegertracing.thriftjava.SpanRef;
import io.jaegertracing.thriftjava.SpanRefType;
import io.jaegertracing.thriftjava.Tag;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.StatusCode;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
  void testThriftSpans() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs);
    List<SpanData> spans = Collections.singletonList(span);

    List<io.jaegertracing.thriftjava.Span> jaegerSpans = Adapter.toJaeger(spans);

    // the span contents are checked somewhere else
    assertEquals(1, jaegerSpans.size());
  }

  @Test
  void testThriftSpan() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs);

    // test
    io.jaegertracing.thriftjava.Span jaegerSpan = Adapter.toJaeger(span);

    String rebuildTraceId =
        TraceId.fromLongs(jaegerSpan.getTraceIdHigh(), jaegerSpan.getTraceIdLow());
    assertThat(rebuildTraceId).isEqualTo(span.getTraceId());
    assertThat(SpanId.fromLong(jaegerSpan.getSpanId())).isEqualTo(span.getSpanId());
    assertThat(jaegerSpan.getOperationName()).isEqualTo("GET /api/endpoint");
    assertThat(jaegerSpan.getStartTime()).isEqualTo(MILLISECONDS.toMicros(startMs));
    assertThat(jaegerSpan.getDuration()).isEqualTo(MILLISECONDS.toMicros(duration));

    assertThat(jaegerSpan.getTagsSize()).isEqualTo(5);
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_KIND).getVStr()).isEqualTo("server");
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_STATUS_CODE).getVLong())
        .isEqualTo(0);
    assertThat(getValue(jaegerSpan.getTags(), Adapter.KEY_SPAN_STATUS_MESSAGE).getVStr())
        .isEqualTo("ok!");

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
  void testJaegerLogs() {
    // prepare
    Event eventsData = getTimedEvent();

    // test
    Collection<Log> logs = Adapter.toJaegerLogs(Collections.singletonList(eventsData));

    // verify
    assertEquals(1, logs.size());
  }

  @Test
  void testJaegerLog() {
    // prepare
    Event event = getTimedEvent();

    // test
    Log log = Adapter.toJaegerLog(event);

    // verify
    assertEquals(2, log.getFieldsSize());

    assertThat(getValue(log.getFields(), Adapter.KEY_LOG_EVENT).getVStr())
        .isEqualTo("the log message");
    assertThat(getValue(log.getFields(), "foo").getVStr()).isEqualTo("bar");
    assertThat(getValue(log.getFields(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT)).isNull();
  }

  @Test
  void jaegerLog_droppedAttributes() {
    Event event = getTimedEvent(3);

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
    Link link =
        Link.create(createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    Collection<SpanRef> spanRefs = Adapter.toSpanRefs(Collections.singletonList(link));

    // verify
    assertEquals(1, spanRefs.size()); // the actual span ref is tested in another test
  }

  @Test
  void testSpanRef() {
    // prepare
    Link link = Link.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    SpanRef spanRef = Adapter.toSpanRef(link);

    // verify
    assertThat(SpanId.fromLong(spanRef.getSpanId())).isEqualTo(SPAN_ID);
    assertThat(TraceId.fromLongs(spanRef.getTraceIdHigh(), spanRef.getTraceIdLow()))
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
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint")
            .setStartEpochNanos(MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(MILLISECONDS.toNanos(endMs))
            .setKind(Span.Kind.SERVER)
            .setStatus(Status.error())
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    assertNotNull(Adapter.toJaeger(span));
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
            .setStartEpochNanos(MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(MILLISECONDS.toNanos(endMs))
            .setKind(Span.Kind.SERVER)
            .setStatus(Status.error())
            .setAttributes(attributes)
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    io.jaegertracing.thriftjava.Span jaegerSpan = Adapter.toJaeger(span);
    assertThat(getValue(jaegerSpan.getTags(), "error.type").getVStr())
        .isEqualTo(this.getClass().getName());
    assertThat(getValue(jaegerSpan.getTags(), "error").isVBool()).isTrue();
  }

  private static Event getTimedEvent() {
    return getTimedEvent(-1);
  }

  private static Event getTimedEvent(int totalAttributeCount) {
    long epochNanos = MILLISECONDS.toNanos(System.currentTimeMillis());
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
        .setParentSpanId(PARENT_SPAN_ID)
        .setName("GET /api/endpoint")
        .setStartEpochNanos(MILLISECONDS.toNanos(startMs))
        .setEndEpochNanos(MILLISECONDS.toNanos(endMs))
        .setAttributes(Attributes.of(booleanKey("valueB"), true))
        .setEvents(Collections.singletonList(getTimedEvent()))
        .setTotalRecordedEvents(1)
        .setLinks(Collections.singletonList(link))
        .setTotalRecordedLinks(1)
        .setKind(Span.Kind.SERVER)
        .setResource(Resource.create(Attributes.empty()))
        .setStatus(Status.create(StatusCode.OK, "ok!"))
        .build();
  }

  private static SpanContext createSpanContext(String traceId, String spanId) {
    return SpanContext.create(
        traceId, spanId, TraceFlags.getDefault(), TraceState.builder().build());
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
        assertThat(TraceId.fromLongs(spanRef.getTraceIdHigh(), spanRef.getTraceIdLow()))
            .isEqualTo(LINK_TRACE_ID);
        assertThat(SpanId.fromLong(spanRef.getSpanId())).isEqualTo(LINK_SPAN_ID);
        found = true;
      }
    }
    assertThat(found).isTrue();
  }

  private static void assertHasParent(io.jaegertracing.thriftjava.Span jaegerSpan) {
    boolean found = false;
    for (SpanRef spanRef : jaegerSpan.getReferences()) {
      if (SpanRefType.CHILD_OF.equals(spanRef.getRefType())) {
        assertThat(TraceId.fromLongs(spanRef.getTraceIdHigh(), spanRef.getTraceIdLow()))
            .isEqualTo(TRACE_ID);
        assertThat(SpanId.fromLong(spanRef.getSpanId())).isEqualTo(PARENT_SPAN_ID);
        found = true;
      }
    }
    assertThat(found).isTrue();
  }
}
