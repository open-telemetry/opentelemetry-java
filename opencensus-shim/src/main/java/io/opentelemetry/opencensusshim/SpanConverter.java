/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.common.Function;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.TraceStateBuilder;
import java.util.Map;
import javax.annotation.Nullable;

class SpanConverter {
  public static final String MESSAGE_EVENT_ATTRIBUTE_KEY_TYPE = "message.event.type";
  public static final String MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_UNCOMPRESSED =
      "message.event.size.uncompressed";
  public static final String MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_COMPRESSED =
      "message.event.size.compressed";

  private SpanConverter() {}

  static Kind mapKind(@Nullable io.opencensus.trace.Span.Kind ocKind) {
    if (ocKind == null) {
      return Kind.INTERNAL;
    }
    switch (ocKind) {
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
    return new OpenTelemetrySpanImpl(otSpan);
  }

  static SpanContext mapSpanContext(io.opentelemetry.api.trace.SpanContext otelSpanContext) {
    return SpanContext.create(
        TraceId.fromLowerBase16(otelSpanContext.getTraceIdAsHexString()),
        SpanId.fromLowerBase16(otelSpanContext.getSpanIdAsHexString()),
        TraceOptions.builder().setIsSampled(otelSpanContext.isSampled()).build(),
        mapTracestate(otelSpanContext.getTraceState()));
  }

  static io.opentelemetry.api.trace.SpanContext mapSpanContext(SpanContext ocSpanContext) {
    return io.opentelemetry.api.trace.SpanContext.create(
        ocSpanContext.getTraceId().toLowerBase16(),
        ocSpanContext.getSpanId().toLowerBase16(),
        ocSpanContext.getTraceOptions().isSampled()
            ? TraceFlags.getSampled()
            : TraceFlags.getDefault(),
        mapTracestate(ocSpanContext.getTracestate()));
  }

  private static TraceState mapTracestate(Tracestate tracestate) {
    TraceStateBuilder builder = TraceState.builder();
    tracestate.getEntries().forEach(entry -> builder.set(entry.getKey(), entry.getValue()));
    return builder.build();
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
