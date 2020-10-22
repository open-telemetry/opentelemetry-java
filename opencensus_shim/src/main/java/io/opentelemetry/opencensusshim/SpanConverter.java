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
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Attributes.Builder;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.Tracer;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class SpanConverter {

  public static class FakeSpan extends Span {

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

  static final long NANOS_PER_SECOND = (long) 1e9;
  private static final Tracer TRACER =
      OpenTelemetry.getTracer("io.opencensus.opentelemetry.migration");

  private SpanConverter() {}

  static io.opentelemetry.trace.Span toOtelSpan(Span span) {
    if (span == null) {
      return null;
    }
    SpanData spanData = ((RecordEventsSpanImpl) span).toSpanData();
    io.opentelemetry.trace.Span.Builder builder =
        TRACER
            .spanBuilder(spanData.getName())
            .setStartTimestamp(
                spanData.getStartTimestamp().getSeconds() * NANOS_PER_SECOND
                    + spanData.getStartTimestamp().getNanos());
    if (spanData.getKind() != null) {
      builder.setSpanKind(mapKind(spanData.getKind()));
    }
    if (spanData.getAttributes() != null) {
      for (Map.Entry<String, AttributeValue> attribute :
          spanData.getAttributes().getAttributeMap().entrySet()) {
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
    if (spanData.getLinks() != null) {
      for (Link link : spanData.getLinks().getLinks()) {
        Builder attributesBuilder = Attributes.builder();
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
            io.opentelemetry.trace.SpanContext.create(
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
    if (kind == null) {
      return null;
    }
    switch (kind) {
      case CLIENT:
        return Kind.CLIENT;
      case SERVER:
        return Kind.SERVER;
      default:
        return null;
    }
  }

  static Span fromOtelSpan(io.opentelemetry.trace.Span otSpan) {
    if (otSpan == null) {
      return null;
    }
    SpanContext spanContext =
        SpanContext.create(
            io.opencensus.trace.TraceId.fromLowerBase16(
                otSpan.getContext().getTraceIdAsHexString()),
            io.opencensus.trace.SpanId.fromLowerBase16(otSpan.getContext().getSpanIdAsHexString()),
            TraceOptions.builder().setIsSampled(otSpan.getContext().isSampled()).build(),
            mapTracestate(otSpan.getContext().getTraceState()));
    return new FakeSpan(spanContext);
  }

  private static Tracestate mapTracestate(TraceState traceState) {
    Tracestate.Builder tracestateBuilder = Tracestate.builder();
    traceState
        .getEntries()
        .forEach(entry -> tracestateBuilder.set(entry.getKey(), entry.getValue()));
    return tracestateBuilder.build();
  }

  static void mapAndAddTimedEvents(
      io.opentelemetry.trace.Span span, List<TimedEvent<MessageEvent>> events) {
    for (TimedEvent<MessageEvent> event : events) {
      span.addEvent(
          String.valueOf(event.getEvent().getMessageId()),
          Attributes.of(
              AttributeKey.stringKey("message.event.type"),
              event.getEvent().getType().toString(),
              AttributeKey.longKey("message.event.size.uncompressed"),
              event.getEvent().getUncompressedMessageSize(),
              AttributeKey.longKey("message.event.size.compressed"),
              event.getEvent().getCompressedMessageSize()),
          event.getTimestamp().getSeconds() * NANOS_PER_SECOND + event.getTimestamp().getNanos());
    }
  }

  static void mapAndAddAnnotations(
      io.opentelemetry.trace.Span span, List<TimedEvent<Annotation>> annotations) {
    for (TimedEvent<Annotation> annotation : annotations) {
      Attributes.Builder attributesBuilder = Attributes.builder();
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
          annotation.getTimestamp().getSeconds() * NANOS_PER_SECOND
              + annotation.getTimestamp().getNanos());
    }
  }

  private static Function<String, Void> setStringAttribute(Attributes.Builder builder, String key) {
    return arg -> {
      builder.setAttribute(key, arg);
      return null;
    };
  }

  private static Function<String, Void> setStringAttribute(
      io.opentelemetry.trace.Span.Builder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  private static Function<Boolean, Void> setBooleanAttribute(
      Attributes.Builder builder, String key) {
    return arg -> {
      builder.setAttribute(key, arg);
      return null;
    };
  }

  private static Function<Boolean, Void> setBooleanAttribute(
      io.opentelemetry.trace.Span.Builder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  private static Function<Long, Void> setLongAttribute(Attributes.Builder builder, String key) {
    return arg -> {
      builder.setAttribute(key, arg);
      return null;
    };
  }

  private static Function<Long, Void> setLongAttribute(
      io.opentelemetry.trace.Span.Builder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  private static Function<Double, Void> setDoubleAttribute(Attributes.Builder builder, String key) {
    return arg -> {
      builder.setAttribute(key, arg);
      return null;
    };
  }

  private static Function<Double, Void> setDoubleAttribute(
      io.opentelemetry.trace.Span.Builder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }
}
