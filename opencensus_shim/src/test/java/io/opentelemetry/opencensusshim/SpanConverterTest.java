/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN;
import static io.opencensus.trace.Span.Kind.CLIENT;
import static io.opentelemetry.opencensusshim.SpanConverter.NANOS_PER_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.internal.ZeroTimeClock;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.RandomIdsGenerator;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceState;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SpanConverterTest {
  private static final RandomIdsGenerator RANDOM_IDS_GENERATOR = new RandomIdsGenerator();

  @Test
  void testToOtelSpan() {
    RecordEventsSpanImpl span = createOpenCensusSpan();

    Span otelSpan = SpanConverter.toOtelSpan(span);

    assertEquals(0x1, otelSpan.getContext().getTraceFlags());
    assertTrue(otelSpan.isRecording());
  }

  @Test
  void testFromOtelSpan() {
    String traceIdHex = RANDOM_IDS_GENERATOR.generateTraceId();
    String spanIdHex = RANDOM_IDS_GENERATOR.generateSpanId();
    String traceStateKey = "key123";
    String traceStateValue = "value123";

    Span otelSpan = Mockito.mock(Span.class);
    when(otelSpan.getContext())
        .thenReturn(
            io.opentelemetry.trace.SpanContext.create(
                traceIdHex,
                spanIdHex,
                TraceFlags.getSampled(),
                TraceState.builder().set(traceStateKey, traceStateValue).build()));

    io.opencensus.trace.Span ocSPan = SpanConverter.fromOtelSpan(otelSpan);

    SpanContext context = ocSPan.getContext();
    assertEquals(traceIdHex, context.getTraceId().toLowerBase16());
    assertEquals(spanIdHex, context.getSpanId().toLowerBase16());
    assertTrue(context.getTraceOptions().isSampled());
    assertEquals(1, context.getTracestate().getEntries().size());
    assertEquals(traceStateValue, context.getTracestate().get(traceStateKey));
  }

  @Test
  void testMapAndAddAnnotation() {
    RecordEventsSpanImpl openCensusSpan = createOpenCensusSpan();
    openCensusSpan.addAnnotation(
        "First annotation!",
        ImmutableMap.of(
            "Attribute1",
            AttributeValue.booleanAttributeValue(false),
            "Attribute2",
            AttributeValue.longAttributeValue(123)));
    openCensusSpan.addAnnotation(
        "Second annotation!",
        ImmutableMap.of(
            "Attribute1",
            AttributeValue.doubleAttributeValue(2.34),
            "Attribute2",
            AttributeValue.stringAttributeValue("attributeValue")));

    Span spanSpy = spy(Span.class);
    List<TimedEvent<Annotation>> annotations =
        openCensusSpan.toSpanData().getAnnotations().getEvents();
    SpanConverter.mapAndAddAnnotations(spanSpy, annotations);

    verify(spanSpy, times(1))
        .addEvent(
            "First annotation!",
            Attributes.builder()
                .setAttribute("Attribute1", false)
                .setAttribute("Attribute2", 123)
                .build(),
            annotations.get(0).getTimestamp().getSeconds() * NANOS_PER_SECOND
                + annotations.get(0).getTimestamp().getNanos());
    verify(spanSpy, times(1))
        .addEvent(
            "Second annotation!",
            Attributes.builder()
                .setAttribute("Attribute1", 2.34)
                .setAttribute("Attribute2", "attributeValue")
                .build(),
            annotations.get(1).getTimestamp().getSeconds() * NANOS_PER_SECOND
                + annotations.get(1).getTimestamp().getNanos());
  }

  @Test
  void testMapAndAddTimedEvents() {
    RecordEventsSpanImpl openCensusSpan = createOpenCensusSpan();
    openCensusSpan.addMessageEvent(
        MessageEvent.builder(MessageEvent.Type.RECEIVED, 1)
            .setCompressedMessageSize(34)
            .setUncompressedMessageSize(80)
            .build());
    openCensusSpan.addMessageEvent(
        MessageEvent.builder(MessageEvent.Type.SENT, 8)
            .setCompressedMessageSize(180)
            .setUncompressedMessageSize(280)
            .build());

    Span spanSpy = spy(Span.class);
    List<TimedEvent<MessageEvent>> messageEvents =
        openCensusSpan.toSpanData().getMessageEvents().getEvents();
    SpanConverter.mapAndAddTimedEvents(spanSpy, messageEvents);

    verify(spanSpy, times(1))
        .addEvent(
            "1",
            Attributes.builder()
                .setAttribute("message.event.type", "RECEIVED")
                .setAttribute("message.event.size.uncompressed", 80)
                .setAttribute("message.event.size.compressed", 34)
                .build(),
            messageEvents.get(0).getTimestamp().getSeconds() * NANOS_PER_SECOND
                + messageEvents.get(0).getTimestamp().getNanos());
    verify(spanSpy, times(1))
        .addEvent(
            "8",
            Attributes.builder()
                .setAttribute("message.event.type", "SENT")
                .setAttribute("message.event.size.uncompressed", 280)
                .setAttribute("message.event.size.compressed", 180)
                .build(),
            messageEvents.get(1).getTimestamp().getSeconds() * NANOS_PER_SECOND
                + messageEvents.get(1).getTimestamp().getNanos());
  }

  private static RecordEventsSpanImpl createOpenCensusSpan() {
    ZeroTimeClock clock = ZeroTimeClock.getInstance();
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            SpanContext.create(
                TraceId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateTraceId()),
                SpanId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateSpanId()),
                TraceOptions.builder().setIsSampled(true).build(),
                Tracestate.builder().set("key123", "value456").build()),
            "Span Name",
            CLIENT,
            SpanId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateSpanId()),
            true,
            TraceParams.DEFAULT,
            new OpenTelemetryStartEndHandler(),
            TimestampConverter.now(clock),
            clock);
    span.putAttribute("boolAttributeKey", AttributeValue.booleanAttributeValue(true));
    span.putAttribute("longAttributeKey", AttributeValue.longAttributeValue(123));
    span.putAttribute("stringAttributeKey", AttributeValue.stringAttributeValue("attributeValue"));
    span.addLink(
        Link.fromSpanContext(
            SpanContext.create(
                TraceId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateTraceId()),
                SpanId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateSpanId()),
                TraceOptions.DEFAULT,
                Tracestate.builder().build()),
            PARENT_LINKED_SPAN));
    span.addLink(
        Link.fromSpanContext(
            SpanContext.create(
                TraceId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateTraceId()),
                SpanId.fromLowerBase16(RANDOM_IDS_GENERATOR.generateSpanId()),
                TraceOptions.DEFAULT,
                Tracestate.builder().build()),
            PARENT_LINKED_SPAN));
    return span;
  }
}
