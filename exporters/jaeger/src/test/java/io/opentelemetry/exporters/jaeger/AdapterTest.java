/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.extensions.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TestSpanData;
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
    assertEquals(1, jaegerSpans.size());
  }

  @Test
  void testProtoSpan() {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;

    SpanData span = getSpanData(startMs, endMs);

    // test
    Model.Span jaegerSpan = Adapter.toJaeger(span);
    assertEquals(TraceProtoUtils.toProtoTraceId(span.getTraceId()), jaegerSpan.getTraceId());
    assertEquals(TraceProtoUtils.toProtoSpanId(span.getSpanId()), jaegerSpan.getSpanId());
    assertEquals("GET /api/endpoint", jaegerSpan.getOperationName());
    assertEquals(Timestamps.fromMillis(startMs), jaegerSpan.getStartTime());
    assertEquals(duration, Durations.toMillis(jaegerSpan.getDuration()));

    assertEquals(5, jaegerSpan.getTagsCount());
    Model.KeyValue keyValue = getValue(jaegerSpan.getTagsList(), Adapter.KEY_SPAN_KIND);
    assertNotNull(keyValue);
    assertEquals("server", keyValue.getVStr());
    keyValue = getValue(jaegerSpan.getTagsList(), Adapter.KEY_SPAN_STATUS_CODE);
    assertNotNull(keyValue);
    assertEquals(0, keyValue.getVInt64());
    assertEquals(Model.ValueType.INT64, keyValue.getVType());
    keyValue = getValue(jaegerSpan.getTagsList(), Adapter.KEY_SPAN_STATUS_MESSAGE);
    assertNotNull(keyValue);
    assertEquals("", keyValue.getVStr());

    assertEquals(1, jaegerSpan.getLogsCount());
    Model.Log log = jaegerSpan.getLogs(0);
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_LOG_EVENT);
    assertNotNull(keyValue);
    assertEquals("the log message", keyValue.getVStr());
    keyValue = getValue(log.getFieldsList(), "foo");
    assertNotNull(keyValue);
    assertEquals("bar", keyValue.getVStr());

    assertEquals(2, jaegerSpan.getReferencesCount());

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
    assertEquals(1, logs.size());
  }

  @Test
  void testJaegerLog() {
    // prepare
    Event event = getTimedEvent();

    // test
    Model.Log log = Adapter.toJaegerLog(event);

    // verify
    assertEquals(2, log.getFieldsCount());

    Model.KeyValue keyValue = getValue(log.getFieldsList(), Adapter.KEY_LOG_EVENT);
    assertNotNull(keyValue);
    assertEquals("the log message", keyValue.getVStr());
    keyValue = getValue(log.getFieldsList(), "foo");
    assertNotNull(keyValue);
    assertEquals("bar", keyValue.getVStr());
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT);
    assertNull(keyValue);

    // verify dropped_attributes_count
    event = getTimedEvent(3);
    log = Adapter.toJaegerLog(event);
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_EVENT_DROPPED_ATTRIBUTES_COUNT);
    assertNotNull(keyValue);
    assertEquals(2, keyValue.getVInt64());
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
    assertTrue(kvB.getVBool());
    assertEquals(Model.ValueType.BOOL, kvB.getVType());
    assertEquals(1., kvD.getVFloat64(), 0);
    assertEquals(Model.ValueType.FLOAT64, kvD.getVType());
    assertEquals(2, kvI.getVInt64());
    assertEquals(Model.ValueType.INT64, kvI.getVType());
    assertEquals("foobar", kvS.getVStr());
    assertEquals("foobar", kvS.getVStrBytes().toStringUtf8());
    assertEquals(Model.ValueType.STRING, kvS.getVType());
    assertEquals("[true,false]", kvArrayB.getVStr());
    assertEquals("[true,false]", kvArrayB.getVStrBytes().toStringUtf8());
    assertEquals(Model.ValueType.STRING, kvArrayB.getVType());
    assertEquals("[1.2345,6.789]", kvArrayD.getVStr());
    assertEquals("[1.2345,6.789]", kvArrayD.getVStrBytes().toStringUtf8());
    assertEquals(Model.ValueType.STRING, kvArrayD.getVType());
    assertEquals("[12345,67890]", kvArrayI.getVStr());
    assertEquals("[12345,67890]", kvArrayI.getVStrBytes().toStringUtf8());
    assertEquals(Model.ValueType.STRING, kvArrayI.getVType());
    assertEquals("[\"foobar\",\"barfoo\"]", kvArrayS.getVStr());
    assertEquals("[\"foobar\",\"barfoo\"]", kvArrayS.getVStrBytes().toStringUtf8());
    assertEquals(Model.ValueType.STRING, kvArrayS.getVType());
  }

  @Test
  void testSpanRefs() {
    // prepare
    Link link =
        Link.create(createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    Collection<Model.SpanRef> spanRefs = Adapter.toSpanRefs(Collections.singletonList(link));

    // verify
    assertEquals(1, spanRefs.size()); // the actual span ref is tested in another test
  }

  @Test
  void testSpanRef() {
    // prepare
    Link link = Link.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    Model.SpanRef spanRef = Adapter.toSpanRef(link);

    // verify
    assertEquals(TraceProtoUtils.toProtoSpanId(SPAN_ID), spanRef.getSpanId());
    assertEquals(TraceProtoUtils.toProtoTraceId(TRACE_ID), spanRef.getTraceId());
    assertEquals(Model.SpanRefType.FOLLOWS_FROM, spanRef.getRefType());
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
    assertNotNull(errorType);
    assertEquals(this.getClass().getName(), errorType.getVStr());
    Model.KeyValue error = getValue(jaegerSpan.getTagsList(), "error");
    assertNotNull(error);
    assertTrue(error.getVBool());
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
        .setParentSpanId(PARENT_SPAN_ID)
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
        assertEquals(TraceProtoUtils.toProtoTraceId(LINK_TRACE_ID), spanRef.getTraceId());
        assertEquals(TraceProtoUtils.toProtoSpanId(LINK_SPAN_ID), spanRef.getSpanId());
        found = true;
      }
    }
    assertTrue(found, "Should have found the follows-from reference");
  }

  private static void assertHasParent(Model.Span jaegerSpan) {
    boolean found = false;
    for (Model.SpanRef spanRef : jaegerSpan.getReferencesList()) {
      if (Model.SpanRefType.CHILD_OF.equals(spanRef.getRefType())) {
        assertEquals(TraceProtoUtils.toProtoTraceId(TRACE_ID), spanRef.getTraceId());
        assertEquals(TraceProtoUtils.toProtoSpanId(PARENT_SPAN_ID), spanRef.getSpanId());
        found = true;
      }
    }
    assertTrue(found, "Should have found the parent reference");
  }
}
