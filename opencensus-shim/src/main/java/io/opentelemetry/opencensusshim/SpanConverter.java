/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.common.Function;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class SpanConverter {

  /**
   * FakeSpan is used to represent OpenTelemetry spans in the OpenCensus context. Only the trace ID,
   * span ID, trace options, and trace state are mapped, so that the correct context information can
   * be picked up by the child spans. FakeSpan does not record events or links.
   */
  static class FakeSpan extends Span {

    private static final EnumSet<Options> RECORD_EVENTS_SPAN_OPTIONS =
        EnumSet.of(Options.RECORD_EVENTS);

    protected FakeSpan(SpanContext context) {
      super(context, RECORD_EVENTS_SPAN_OPTIONS);
    }

    @Override
    public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}

    @Override
    public void addAnnotation(Annotation annotation) {}

    @Override
    public void addLink(Link link) {}

    @Override
    public void end(EndSpanOptions options) {}
  }

  public static final String MESSAGE_EVENT_ATTRIBUTE_KEY_TYPE = "message.event.type";
  public static final String MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_UNCOMPRESSED =
      "message.event.size.uncompressed";
  public static final String MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_COMPRESSED =
      "message.event.size.compressed";

  private static final Tracer TRACER =
      OpenTelemetry.getGlobalTracer("io.opencensus.opentelemetry.migration");

  private SpanConverter() {}

  static io.opentelemetry.api.trace.Span toOtelSpan(Span span) {
    if (span == null) {
      return io.opentelemetry.api.trace.Span.getInvalid();
    }
    SpanData ocSpanData = ((RecordEventsSpanImpl) span).toSpanData();
    SpanBuilder builder =
        TRACER
            .spanBuilder(ocSpanData.getName())
            .setStartTimestamp(
                TimeUnit.SECONDS.toNanos(ocSpanData.getStartTimestamp().getSeconds())
                    + ocSpanData.getStartTimestamp().getNanos());
    if (ocSpanData.getKind() != null) {
      builder.setSpanKind(mapKind(ocSpanData.getKind()));
    }
    if (ocSpanData.getAttributes() != null) {
      for (Map.Entry<String, AttributeValue> attribute :
          ocSpanData.getAttributes().getAttributeMap().entrySet()) {
        attribute
            .getValue()
            .match(
                setStringAttribute(builder, attribute),
                setBooleanAttribute(builder, attribute),
                setLongAttribute(builder, attribute),
                setDoubleAttribute(builder, attribute),
                arg -> null);
      }
    }
    if (ocSpanData.getLinks() != null) {
      for (Link link : ocSpanData.getLinks().getLinks()) {
        AttributesBuilder attributesBuilder = Attributes.builder();
        link.getAttributes()
            .forEach(
                (s, attributeValue) ->
                    attributeValue.match(
                        setStringAttribute(attributesBuilder, s),
                        setBooleanAttribute(attributesBuilder, s),
                        setLongAttribute(attributesBuilder, s),
                        setDoubleAttribute(attributesBuilder, s),
                        arg -> null));
        builder.addLink(
            io.opentelemetry.api.trace.SpanContext.create(
                TraceId.bytesToHex(link.getTraceId().getBytes()),
                SpanId.bytesToHex(link.getSpanId().getBytes()),
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            attributesBuilder.build());
      }
    }
    return builder.startSpan();
  }

  private static Kind mapKind(Span.Kind kind) {
    switch (kind) {
      case CLIENT:
        return Kind.CLIENT;
      case SERVER:
        return Kind.SERVER;
    }
    return Kind.INTERNAL;
  }

  static Span fromOtelSpan(io.opentelemetry.api.trace.Span otSpan) {
    if (otSpan == null) {
      return null;
    }
    SpanContext spanContext =
        SpanContext.create(
            io.opencensus.trace.TraceId.fromLowerBase16(
                otSpan.getSpanContext().getTraceIdAsHexString()),
            io.opencensus.trace.SpanId.fromLowerBase16(
                otSpan.getSpanContext().getSpanIdAsHexString()),
            TraceOptions.builder().setIsSampled(otSpan.getSpanContext().isSampled()).build(),
            mapTracestate(otSpan.getSpanContext().getTraceState()));
    return new FakeSpan(spanContext);
  }

  private static Tracestate mapTracestate(TraceState traceState) {
    Tracestate.Builder tracestateBuilder = Tracestate.builder();
    traceState.forEach(tracestateBuilder::set);
    return tracestateBuilder.build();
  }

  static void mapAndAddTimedEvents(
      io.opentelemetry.api.trace.Span span, List<TimedEvent<MessageEvent>> events) {
    for (TimedEvent<MessageEvent> event : events) {
      span.addEvent(
          String.valueOf(event.getEvent().getMessageId()),
          Attributes.of(
              AttributeKey.stringKey(MESSAGE_EVENT_ATTRIBUTE_KEY_TYPE),
              event.getEvent().getType().toString(),
              AttributeKey.longKey(MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_UNCOMPRESSED),
              event.getEvent().getUncompressedMessageSize(),
              AttributeKey.longKey(MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_COMPRESSED),
              event.getEvent().getCompressedMessageSize()),
          TimeUnit.SECONDS.toNanos(event.getTimestamp().getSeconds())
              + event.getTimestamp().getNanos());
    }
  }

  static void mapAndAddAnnotations(
      io.opentelemetry.api.trace.Span span, List<TimedEvent<Annotation>> annotations) {
    for (TimedEvent<Annotation> annotation : annotations) {
      AttributesBuilder attributesBuilder = Attributes.builder();
      annotation
          .getEvent()
          .getAttributes()
          .forEach(
              (s, attributeValue) ->
                  attributeValue.match(
                      setStringAttribute(attributesBuilder, s),
                      setBooleanAttribute(attributesBuilder, s),
                      setLongAttribute(attributesBuilder, s),
                      setDoubleAttribute(attributesBuilder, s),
                      arg -> null));
      span.addEvent(
          annotation.getEvent().getDescription(),
          attributesBuilder.build(),
          TimeUnit.SECONDS.toNanos(annotation.getTimestamp().getSeconds())
              + annotation.getTimestamp().getNanos());
    }
  }

  private static Function<String, Void> setStringAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  private static Function<String, Void> setStringAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  private static Function<Boolean, Void> setBooleanAttribute(
      AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  private static Function<Boolean, Void> setBooleanAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  private static Function<Long, Void> setLongAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  private static Function<Long, Void> setLongAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  private static Function<Double, Void> setDoubleAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  private static Function<Double, Void> setDoubleAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }
}
