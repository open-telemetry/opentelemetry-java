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

package io.opentelemetry.sdk.contrib.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.util.Samplers;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InMemoryTracing}. */
@RunWith(JUnit4.class)
public class InMemoryTracingTest {
  InMemoryTracing tracing = new InMemoryTracing();
  final SpanData.Timestamp testTimestamp =
      SpanData.Timestamp.fromMillis(System.currentTimeMillis());

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultTracer() {
    assertThat(tracing.getTracer()).isInstanceOf(TracerSdk.class);
  }

  @Test
  public void defaultFinishedSpanItems() {
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
  }

  @Test
  public void ctor_tracer() {
    TracerSdk tracer = new TracerSdk();
    InMemoryTracing tracing = new InMemoryTracing(tracer);
    assertThat(tracing.getTracer()).isSameInstanceAs(tracer);
  }

  @Test
  public void ctor_nullTracer() {
    thrown.expect(NullPointerException.class);
    new InMemoryTracing(null);
  }

  @Test
  public void getFinishedSpanItems() {
    tracing.getTracer().spanBuilder("A").startSpan().end();
    tracing.getTracer().spanBuilder("B").startSpan().end();

    List<Span> finishedSpanItems = tracing.getFinishedSpanItems();
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
    assertThat(finishedSpanItems.get(1).getName()).isEqualTo("B");
  }

  @Test
  public void getFinishedSpans_sampled() {
    tracing.getTracer().spanBuilder("A").startSpan().end();
    tracing.getTracer().spanBuilder("B").setSampler(Samplers.neverSample()).startSpan().end();

    List<Span> finishedSpanItems = tracing.getFinishedSpanItems();
    assertThat(finishedSpanItems).hasSize(1);
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");

    List<SpanData> finishedSpanDataItems = tracing.getFinishedSpanDataItems();
    assertThat(finishedSpanDataItems).hasSize(1);
    assertThat(finishedSpanDataItems.get(0).getName()).isEqualTo("A");
  }

  @Test
  public void getFinishedSpanDataItems() {
    tracing.getTracer().spanBuilder("A").startSpan().end();
    tracing.getTracer().spanBuilder("B").startSpan().end();

    List<SpanData> finishedSpanDataItems = tracing.getFinishedSpanDataItems();
    assertThat(finishedSpanDataItems).hasSize(2);
    assertThat(finishedSpanDataItems.get(0).getName()).isEqualTo("A");
    assertThat(finishedSpanDataItems.get(1).getName()).isEqualTo("B");
  }

  @Test
  public void getFinishedSpanDataItems_simpleData() {
    io.opentelemetry.trace.Span span = createSimpleSpan("A");

    List<SpanData> finishedSpanItems = tracing.getFinishedSpanDataItems();
    assertThat(finishedSpanItems.size()).isEqualTo(1);

    SpanData spanData = finishedSpanItems.get(0);
    assertThat(spanData.getName()).isEqualTo("A");
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getContext().getTraceId()).isEqualTo(span.getContext().getTraceId());
    assertThat(spanData.getContext().getSpanId()).isEqualTo(span.getContext().getSpanId());
    assertThat(spanData.getContext().getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(spanData.getContext().getTracestate()).isEqualTo(Tracestate.getDefault());
    assertThat(spanData.getResource()).isEqualTo(Resource.getEmpty());
    assertThat(spanData.getKind()).isEqualTo(Kind.INTERNAL);
    assertThat(spanData.getStatus()).isEqualTo(Status.OK);
    assertThat(spanData.getStartTimestamp().getSeconds() >= testTimestamp.getSeconds()).isTrue();
    assertThat(spanData.getEndTimestamp().getSeconds() >= testTimestamp.getSeconds()).isTrue();
    assertThat(spanData.getEndTimestamp().getSeconds() >= spanData.getStartTimestamp().getSeconds())
        .isTrue();

    assertThat(spanData.getAttributes()).isEmpty();
    assertThat(spanData.getTimedEvents()).isEmpty();
    assertThat(spanData.getLinks()).isEmpty();
  }

  @Test
  public void getFinishedSpanDataItems_fullData() {
    io.opentelemetry.trace.Span parentSpan = createSimpleSpan("parent");
    io.opentelemetry.trace.Span extraParentSpan = createSimpleSpan("extraParent");
    tracing.reset();

    io.opentelemetry.trace.Span span =
        tracing
            .getTracer()
            .spanBuilder("A")
            .setParent(parentSpan)
            .setSpanKind(Kind.CONSUMER)
            .startSpan();
    span.addLink(
        extraParentSpan.getContext(),
        createAttrMap(
            "attr1", AttributeValue.stringAttributeValue("value1"),
            "attr2", AttributeValue.stringAttributeValue("value2")));
    span.addEvent(
        "MyEvent",
        createAttrMap(
            "attr1", AttributeValue.stringAttributeValue("value1"),
            "attr2", AttributeValue.stringAttributeValue("value2")));
    span.setAttribute("boolAttribute", true);
    span.setAttribute("longAttribute", 1);
    span.setAttribute("doubleAttribute", 1.0);
    span.setAttribute("stringAttribute", "1.0");
    span.setStatus(Status.UNAUTHENTICATED.withDescription("No login"));
    span.end();

    List<SpanData> finishedSpanItems = tracing.getFinishedSpanDataItems();
    assertThat(finishedSpanItems.size()).isEqualTo(1);

    SpanData spanData = finishedSpanItems.get(0);
    assertThat(spanData.getName()).isEqualTo("A");
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpan.getContext().getSpanId());
    assertThat(spanData.getContext().getTraceId()).isEqualTo(parentSpan.getContext().getTraceId());
    assertThat(spanData.getContext().getSpanId()).isEqualTo(span.getContext().getSpanId());
    assertThat(spanData.getContext().getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(spanData.getContext().getTracestate()).isEqualTo(Tracestate.getDefault());
    assertThat(spanData.getResource()).isEqualTo(Resource.getEmpty());
    assertThat(spanData.getKind()).isEqualTo(Kind.CONSUMER);
    assertThat(spanData.getStatus()).isEqualTo(Status.UNAUTHENTICATED.withDescription("No login"));
    assertThat(spanData.getStartTimestamp().getSeconds() >= testTimestamp.getSeconds()).isTrue();
    assertThat(spanData.getEndTimestamp().getSeconds() >= testTimestamp.getSeconds()).isTrue();
    assertThat(spanData.getEndTimestamp().getSeconds() >= spanData.getStartTimestamp().getSeconds())
        .isTrue();

    Map<String, AttributeValue> attrs = spanData.getAttributes();
    assertThat(attrs).hasSize(4);
    assertThat(attrs.get("boolAttribute").getBooleanValue()).isEqualTo(true);
    assertThat(attrs.get("longAttribute").getLongValue()).isEqualTo(1);
    assertThat(attrs.get("doubleAttribute").getDoubleValue()).isEqualTo(1.0);
    assertThat(attrs.get("stringAttribute").getStringValue()).isEqualTo("1.0");

    List<Link> links = spanData.getLinks();
    assertThat(links.size()).isEqualTo(1);
    Link link = links.get(0);
    assertThat(link.getContext().getTraceId()).isEqualTo(extraParentSpan.getContext().getTraceId());
    assertThat(link.getContext().getSpanId()).isEqualTo(extraParentSpan.getContext().getSpanId());
    assertThat(link.getContext().getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(link.getContext().getTracestate()).isEqualTo(Tracestate.getDefault());
    assertThat(link.getAttributes()).hasSize(2);
    assertThat(link.getAttributes())
        .containsEntry("attr1", AttributeValue.stringAttributeValue("value1"));
    assertThat(link.getAttributes())
        .containsEntry("attr2", AttributeValue.stringAttributeValue("value2"));

    List<SpanData.TimedEvent> events = spanData.getTimedEvents();
    assertThat(events).hasSize(1);
    SpanData.TimedEvent event = events.get(0);
    assertThat(event.getTimestamp().getSeconds() >= testTimestamp.getSeconds()).isTrue();
    assertThat(event.getEvent().getName()).isEqualTo("MyEvent");
    assertThat(event.getEvent().getAttributes())
        .containsEntry("attr1", AttributeValue.stringAttributeValue("value1"));
    assertThat(event.getEvent().getAttributes())
        .containsEntry("attr2", AttributeValue.stringAttributeValue("value2"));
  }

  @Test
  public void reset() {
    tracing.getTracer().spanBuilder("A").startSpan();
    tracing.getTracer().spanBuilder("B").startSpan();
    tracing.reset();
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
    assertThat(tracing.getFinishedSpanDataItems().size()).isEqualTo(0);

    tracing.reset();
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
    assertThat(tracing.getFinishedSpanDataItems().size()).isEqualTo(0);
  }

  io.opentelemetry.trace.Span createSimpleSpan(String name) {
    io.opentelemetry.trace.Span span = tracing.getTracer().spanBuilder(name).startSpan();
    span.end();
    return span;
  }

  static Map<String, AttributeValue> createAttrMap(
      String k1, AttributeValue a1, String k2, AttributeValue a2) {
    Map<String, AttributeValue> attrs = new TreeMap<>();
    attrs.put(k1, a1);
    attrs.put(k2, a2);
    return attrs;
  }
}
