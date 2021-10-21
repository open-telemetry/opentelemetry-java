/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.Common.MAX_TRACE_ID_LENGTH;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the protocol used by OpenTracing Basic Tracers. Context is propagated through 3
 * headers, ot-tracer-traceid, ot-tracer-span-id, and ot-tracer-sampled. IDs are sent as hex strings
 * and sampled is sent as true or false. Baggage values are propagated using the ot-baggage- prefix.
 * See <a
 * href=https://github.com/opentracing/basictracer-python/blob/master/basictracer/text_propagator.py>OT
 * Python Propagation TextMapPropagator</a>.
 */
@Immutable
public final class OtTracePropagator implements TextMapPropagator {

  static final String TRACE_ID_HEADER = "ot-tracer-traceid";
  static final String SPAN_ID_HEADER = "ot-tracer-spanid";
  static final String SAMPLED_HEADER = "ot-tracer-sampled";
  static final String PREFIX_BAGGAGE_HEADER = "ot-baggage-";
  private static final Collection<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER));

  private static final OtTracePropagator INSTANCE = new OtTracePropagator();

  private OtTracePropagator() {
    // singleton
  }

  public static OtTracePropagator getInstance() {
    return INSTANCE;
  }

  @Override
  public Collection<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null || setter == null) {
      return;
    }
    final SpanContext spanContext = Span.fromContext(context).getSpanContext();
    if (!spanContext.isValid()) {
      return;
    }
    // Lightstep trace id MUST be 64-bits therefore OpenTelemetry trace id is truncated to 64-bits
    // by retaining least significant (right-most) bits.
    setter.set(
        carrier, TRACE_ID_HEADER, spanContext.getTraceId().substring(TraceId.getLength() / 2));
    setter.set(carrier, SPAN_ID_HEADER, spanContext.getSpanId());
    setter.set(carrier, SAMPLED_HEADER, String.valueOf(spanContext.isSampled()));

    // Baggage is only injected if there is a valid SpanContext
    Baggage baggage = Baggage.fromContext(context);
    if (!baggage.isEmpty()) {
      // Metadata is not supported by OpenTracing
      baggage.forEach(
          (key, baggageEntry) ->
              setter.set(carrier, PREFIX_BAGGAGE_HEADER + key, baggageEntry.getValue()));
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      return Context.root();
    }
    if (getter == null) {
      return context;
    }
    String incomingTraceId = getter.get(carrier, TRACE_ID_HEADER);
    String traceId =
        incomingTraceId == null
            ? TraceId.getInvalid()
            : StringUtils.padLeft(incomingTraceId, MAX_TRACE_ID_LENGTH);
    String spanId = getter.get(carrier, SPAN_ID_HEADER);
    String sampled = getter.get(carrier, SAMPLED_HEADER);
    SpanContext spanContext = buildSpanContext(traceId, spanId, sampled);
    if (!spanContext.isValid()) {
      return context;
    }

    Context extractedContext = context.with(Span.wrap(spanContext));

    // Baggage is only extracted if there is a valid SpanContext
    if (carrier != null) {
      BaggageBuilder baggageBuilder = Baggage.builder();
      for (String key : getter.keys(carrier)) {
        if (!key.startsWith(PREFIX_BAGGAGE_HEADER)) {
          continue;
        }
        String value = getter.get(carrier, key);
        if (value == null) {
          continue;
        }
        baggageBuilder.put(key.replace(OtTracePropagator.PREFIX_BAGGAGE_HEADER, ""), value);
      }
      Baggage baggage = baggageBuilder.build();
      if (!baggage.isEmpty()) {
        extractedContext = extractedContext.with(baggage);
      }
    }

    return extractedContext;
  }

  private static SpanContext buildSpanContext(
      @Nullable String traceId, @Nullable String spanId, @Nullable String sampled) {
    if (!Common.isTraceIdValid(traceId) || !Common.isSpanIdValid(spanId)) {
      return SpanContext.getInvalid();
    }
    return Common.buildSpanContext(traceId, spanId, sampled);
  }
}
