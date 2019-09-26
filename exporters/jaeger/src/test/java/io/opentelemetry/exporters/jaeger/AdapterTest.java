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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.export.SpanData.Event;
import io.opentelemetry.sdk.trace.export.SpanData.Link;
import io.opentelemetry.sdk.trace.export.SpanData.TimedEvent;
import io.opentelemetry.sdk.trace.export.SpanData.Timestamp;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Test;

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
    Timestamp startTime = toTimestamp(startMs);
    Timestamp endTime = toTimestamp(endMs);

    SpanData span = getSpanData(startTime, endTime);
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
    Timestamp startTime = toTimestamp(startMs);
    Timestamp endTime = toTimestamp(endMs);

    SpanData span = getSpanData(startTime, endTime);

    // test
    Model.Span jaegerSpan = Adapter.toJaeger(span);
    assertEquals(TraceProtoUtils.toProtoTraceId(span.getTraceId()), jaegerSpan.getTraceId());
    assertEquals(TraceProtoUtils.toProtoSpanId(span.getSpanId()), jaegerSpan.getSpanId());
    assertEquals("GET /api/endpoint", jaegerSpan.getOperationName());
    assertEquals(
        com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(startTime.getSeconds())
            .setNanos(startTime.getNanos())
            .build(),
        jaegerSpan.getStartTime());
    assertEquals(duration, jaegerSpan.getDuration().getNanos() / 1000000);

    assertEquals(4, jaegerSpan.getTagsCount());
    assertEquals("SERVER", getValue(jaegerSpan.getTagsList(), "span.kind").getVStr());
    assertEquals(0, getValue(jaegerSpan.getTagsList(), "span.status.code").getVInt64());
    assertEquals("", getValue(jaegerSpan.getTagsList(), "span.status.message").getVStr());

    assertEquals(1, jaegerSpan.getLogsCount());
    Model.Log log = jaegerSpan.getLogs(0);
    assertEquals("the log message", getValue(log.getFieldsList(), "message").getVStr());
    assertEquals("bar", getValue(log.getFieldsList(), "foo").getVStr());

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

    assertEquals("the log message", getValue(log.getFieldsList(), "message").getVStr());
    assertEquals("bar", getValue(log.getFieldsList(), "foo").getVStr());
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

    // test
    Model.KeyValue kvB = Adapter.toKeyValue("valueB", valueB);
    Model.KeyValue kvD = Adapter.toKeyValue("valueD", valueD);
    Model.KeyValue kvI = Adapter.toKeyValue("valueI", valueI);
    Model.KeyValue kvS = Adapter.toKeyValue("valueS", valueS);

    // verify
    assertTrue(kvB.getVBool());
    assertEquals(1., kvD.getVFloat64(), 0);
    assertEquals(2, kvI.getVInt64());
    assertEquals("foobar", kvS.getVStr());
    assertEquals("foobar", kvS.getVStrBytes().toStringUtf8());
  }

  @Test
  public void testSpanRefs() {
    // prepare
    io.opentelemetry.trace.Link link =
        SpanData.Link.create(
            createSpanContext("00000000000000000000000000cba123", "0000000000fed456"));

    // test
    Collection<Model.SpanRef> spanRefs = Adapter.toSpanRefs(Collections.singletonList(link));

    // verify
    assertEquals(1, spanRefs.size()); // the actual span ref is tested in another test
  }

  @Test
  public void testSpanRef() {
    // prepare
    SpanData.Link link = SpanData.Link.create(createSpanContext(TRACE_ID, SPAN_ID));

    // test
    Model.SpanRef spanRef = Adapter.toSpanRef(link);

    // verify
    assertEquals(
        TraceProtoUtils.toProtoSpanId(SpanId.fromLowerBase16(SPAN_ID, 0)), spanRef.getSpanId());
    assertEquals(
        TraceProtoUtils.toProtoTraceId(TraceId.fromLowerBase16(TRACE_ID, 0)), spanRef.getTraceId());
    assertEquals(Model.SpanRefType.FOLLOWS_FROM, spanRef.getRefType());
  }

  private static TimedEvent getTimedEvent() {
    long ms = System.currentTimeMillis();
    Timestamp ts = toTimestamp(ms);
    AttributeValue valueS = AttributeValue.stringAttributeValue("bar");
    ImmutableMap<String, AttributeValue> attributes =
        ImmutableMap.<String, AttributeValue>of("foo", valueS);
    return TimedEvent.create(ts, Event.create("the log message", attributes));
  }

  private static SpanData getSpanData(Timestamp startTime, Timestamp endTime) {
    AttributeValue valueB = AttributeValue.booleanAttributeValue(true);
    Map<String, AttributeValue> attributes =
        ImmutableMap.<String, AttributeValue>of("valueB", valueB);

    io.opentelemetry.trace.Link link =
        Link.create(createSpanContext(LINK_TRACE_ID, LINK_SPAN_ID), attributes);

    return SpanData.newBuilder()
        .context(createSpanContext(TRACE_ID, SPAN_ID))
        .parentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
        .name("GET /api/endpoint")
        .startTimestamp(startTime)
        .endTimestamp(endTime)
        .attributes(attributes)
        .timedEvents(Collections.singletonList(getTimedEvent()))
        .links(Collections.singletonList(link))
        .kind(Span.Kind.SERVER)
        .resource(Resource.create(Collections.<String, String>emptyMap()))
        .status(Status.OK)
        .build();
  }

  private static SpanContext createSpanContext(String traceId, String spanId) {
    return SpanContext.create(
        TraceId.fromLowerBase16(traceId, 0),
        SpanId.fromLowerBase16(spanId, 0),
        TraceFlags.builder().build(),
        Tracestate.builder().build());
  }

  private static Timestamp toTimestamp(long ms) {
    return Timestamp.create(ms / 1000, (int) ((ms % 1000) * 1000000));
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
