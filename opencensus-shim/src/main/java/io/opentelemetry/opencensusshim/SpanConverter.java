/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.common.Function;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.TraceState;
import java.util.EnumSet;
import java.util.Map;

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
    public void putAttribute(String key, AttributeValue value) {}

    @Override
    public void putAttributes(Map<String, AttributeValue> attributes) {}

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

  private SpanConverter() {}

  static Kind mapKind(Span.Kind kind) {
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
            TraceId.fromLowerBase16(otSpan.getSpanContext().getTraceIdAsHexString()),
            SpanId.fromLowerBase16(otSpan.getSpanContext().getSpanIdAsHexString()),
            TraceOptions.builder().setIsSampled(otSpan.getSpanContext().isSampled()).build(),
            mapTracestate(otSpan.getSpanContext().getTraceState()));
    return new FakeSpan(spanContext);
  }

  static Tracestate mapTracestate(TraceState traceState) {
    Tracestate.Builder tracestateBuilder = Tracestate.builder();
    traceState.forEach(tracestateBuilder::set);
    return tracestateBuilder.build();
  }

  static Function<String, Void> setStringAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  static Function<String, Void> setStringAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  static Function<Boolean, Void> setBooleanAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  static Function<Boolean, Void> setBooleanAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  static Function<Long, Void> setLongAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  static Function<Long, Void> setLongAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }

  static Function<Double, Void> setDoubleAttribute(AttributesBuilder builder, String key) {
    return arg -> {
      builder.put(key, arg);
      return null;
    };
  }

  static Function<Double, Void> setDoubleAttribute(
      SpanBuilder builder, Map.Entry<String, AttributeValue> attribute) {
    return arg -> {
      builder.setAttribute(attribute.getKey(), arg);
      return null;
    };
  }
}
