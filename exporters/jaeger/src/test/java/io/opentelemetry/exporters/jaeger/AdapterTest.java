/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.jaeger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.contrib.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.data.SpanData.TimedEvent;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Adapter}. */
@RunWith(JUnit4.class)
public class AdapterTest {

  private static final String LINK_TRACE_ID = "00000000000000000000000000cba123";
  private static final String LINK_SPAN_ID = "0000000000fed456";
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";
  private static final String PARENT_SPAN_ID = "0000000000aef789";

  @Test
  public void testProtoSpans() {
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
  public void testProtoSpan() {
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

    assertEquals(4, jaegerSpan.getTagsCount());
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
    keyValue = getValue(log.getFieldsList(), Adapter.KEY_LOG_MESSAGE);
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
  public void testJaegerLogs() {
    // prepare
    SpanData.TimedEvent timedEvents = getTimedEvent();

    // test
    Collection<Model.Log> logs = Adapter.toJaegerLogs(Collections.singletonList(timedEvents));

    // verify
    assertEquals(1, logs.size());
  }

  @Test
  public void testJaegerLog() {
    // prepare
    SpanData.TimedEvent timedEvent = getTimedEvent();

    // test
    Model.Log log = Adapter.toJaegerLog(timedEvent);

    // verify
    assertEquals(2, log.getFieldsCount());

    Model.KeyValue keyValue = getValue(log.getFieldsList(), Adapter.KEY_LOG_MESSAGE);
    assertNotNull(keyValue);
    assertEquals("the log message", keyValue.getVStr());
    keyValue = getValue(log.getFieldsList(), "foo");
    assertNotNull(keyValue);
    assertEquals("bar", keyValue.getVStr());
  }

  @Test
  public void testKeyValues() {
    // prepare
    AttributeValue valueB = AttributeValue.booleanAttributeValue(true);

    // test
    Collection<Model.KeyValue> keyValues =
        Adapter.toKeyValues(Collections.singletonMap("valueB", valueB));

    // verify
    // the actual content is checked in some other test
    assertEquals(1, keyValues.size());
  }

  @Test
  public void testKeyValue() {
    // prepare
    AttributeValue valueB = AttributeValue.booleanAttributeValue(true);
    AttributeValue valueD = AttributeValue.doubleAttributeValue(1.);
    AttributeValue valueI = AttributeValue.longAttributeValue(2);
    AttributeValue valueS = AttributeValue.stringAttributeValue("foobar");
    AttributeValue valueArrayB = AttributeValue.arrayAttributeValue(true, false);
    AttributeValue valueArrayD = AttributeValue.arrayAttributeValue(1.2345, 6.789);
    AttributeValue valueArrayI = AttributeValue.arrayAttributeValue(12345L, 67890L);
    AttributeValue valueArrayS = AttributeValue.arrayAttributeValue("foobar", "barfoo");

    // test
    Model.KeyValue kvB = Adapter.toKeyValue("valueB", valueB);
    Model.KeyValue kvD = Adapter.toKeyValue("valueD", valueD);
    Model.KeyValue kvI = Adapter.toKeyValue("valueI", valueI);
    Model.KeyValue kvS = Adapter.toKeyValue("valueS", valueS);
    Model.KeyValue kvArrayB = Adapter.toKeyValue("valueArrayB", valueArrayB);
    Model.KeyValue kvArrayD = Adapter.toKeyValue("valueArrayD", valueArrayD);
    Model.KeyValue kvArrayI = Adapter.toKeyValue("valueArrayI", valueArrayI);
    Model.KeyValue kvArrayS = Adapter.toKeyValue("valueArrayS", valueArrayS);

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
  public void testSpanRefs() {
    // prepare
    Link link =
        Link.create(createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    Collection<Model.SpanRef> spanRefs = Adapter.toSpanRefs(Collections.singletonList(link));

    // verify
    assertEquals(1, spanRefs.size()); // the actual span ref is tested in another test
  }

  @Test
  public void testSpanRef() {
    // prepare
    Link link = Link.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    Model.SpanRef spanRef = Adapter.toSpanRef(link);

    // verify
    assertEquals(
        TraceProtoUtils.toProtoSpanId(SpanId.fromLowerBase16(SPAN_ID, 0)), spanRef.getSpanId());
    assertEquals(
        TraceProtoUtils.toProtoTraceId(TraceId.fromLowerBase16(TRACE_ID, 0)), spanRef.getTraceId());
    assertEquals(Model.SpanRefType.FOLLOWS_FROM, spanRef.getRefType());
  }

  @Test
  public void testStatusNotOk() {
    long startMs = System.currentTimeMillis();
    long endMs = startMs + 900;
    SpanData span =
        SpanData.newBuilder()
            .setHasEnded(true)
            .setTraceId(TraceId.fromLowerBase16(TRACE_ID, 0))
            .setSpanId(SpanId.fromLowerBase16(SPAN_ID, 0))
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setKind(Span.Kind.SERVER)
            .setStatus(Status.CANCELLED)
            .setTotalRecordedEvents(0)
            .setTotalRecordedLinks(0)
            .build();

    assertNotNull(Adapter.toJaeger(span));
  }

  @Test
  public void testSpanError() {
    ImmutableMap<String, AttributeValue> attributes =
        ImmutableMap.of(
            "error.type",
            AttributeValue.stringAttributeValue(this.getClass().getName()),
            "error.message",
            AttributeValue.stringAttributeValue("server error"));
    long startMs = System.currentTimeMillis();
    long endMs = startMs + 900;
    SpanData span =
        SpanData.newBuilder()
            .setHasEnded(true)
            .setTraceId(TraceId.fromLowerBase16(TRACE_ID, 0))
            .setSpanId(SpanId.fromLowerBase16(SPAN_ID, 0))
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setKind(Span.Kind.SERVER)
            .setStatus(Status.UNKNOWN)
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
    assertEquals(true, error.getVBool());
  }

  private static TimedEvent getTimedEvent() {
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    AttributeValue valueS = AttributeValue.stringAttributeValue("bar");
    ImmutableMap<String, AttributeValue> attributes = ImmutableMap.of("foo", valueS);
    return TimedEvent.create(epochNanos, "the log message", attributes);
  }

  private static SpanData getSpanData(long startMs, long endMs) {
    AttributeValue valueB = AttributeValue.booleanAttributeValue(true);
    Map<String, AttributeValue> attributes = ImmutableMap.of("valueB", valueB);

    Link link = Link.create(createSpanContext(LINK_TRACE_ID, LINK_SPAN_ID), attributes);

    return SpanData.newBuilder()
        .setHasEnded(true)
        .setTraceId(TraceId.fromLowerBase16(TRACE_ID, 0))
        .setSpanId(SpanId.fromLowerBase16(SPAN_ID, 0))
        .setParentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
        .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
        .setAttributes(attributes)
        .setTimedEvents(Collections.singletonList(getTimedEvent()))
        .setTotalRecordedEvents(1)
        .setLinks(Collections.singletonList(link))
        .setTotalRecordedLinks(1)
        .setKind(Span.Kind.SERVER)
        .setResource(Resource.create(Collections.<String, AttributeValue>emptyMap()))
        .setStatus(Status.OK)
        .build();
  }

  private static SpanContext createSpanContext(String traceId, String spanId) {
    return SpanContext.create(
        TraceId.fromLowerBase16(traceId, 0),
        SpanId.fromLowerBase16(spanId, 0),
        TraceFlags.builder().build(),
        TraceState.builder().build());
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
        assertEquals(
            TraceProtoUtils.toProtoTraceId(TraceId.fromLowerBase16(LINK_TRACE_ID, 0)),
            spanRef.getTraceId());
        assertEquals(
            TraceProtoUtils.toProtoSpanId(SpanId.fromLowerBase16(LINK_SPAN_ID, 0)),
            spanRef.getSpanId());
        found = true;
      }
    }
    assertTrue("Should have found the follows-from reference", found);
  }

  private static void assertHasParent(Model.Span jaegerSpan) {
    boolean found = false;
    for (Model.SpanRef spanRef : jaegerSpan.getReferencesList()) {
      if (Model.SpanRefType.CHILD_OF.equals(spanRef.getRefType())) {
        assertEquals(
            TraceProtoUtils.toProtoTraceId(TraceId.fromLowerBase16(TRACE_ID, 0)),
            spanRef.getTraceId());
        assertEquals(
            TraceProtoUtils.toProtoSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0)),
            spanRef.getSpanId());
        found = true;
      }
    }
    assertTrue("Should have found the parent reference", found);
  }
}
