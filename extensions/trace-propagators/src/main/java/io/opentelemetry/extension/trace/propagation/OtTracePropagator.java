/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.Common.MAX_SPAN_ID_LENGTH;
import static io.opentelemetry.extension.trace.propagation.Common.MAX_TRACE_ID_LENGTH;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the protocol used by OpenTracing Basic Tracers. Context is propagated through 3
 * headers, ot-tracer-traceid, ot-tracer-span-id, and ot-tracer-sampled. IDs are sent as hex strings
 * and sampled is sent as true or false. Baggage values are propagated using the ot-baggage- prefix.
 * See <a
 * href=https://github.com/opentracing/basictracer-python/blob/master/basictracer/text_propagator.py>OT
 * Python Propagation TextMapPropagator</a>.
 *
 * @deprecated the OT trace propagation format is deprecated in the OpenTelemetry specification (see
 *     <a href="https://github.com/open-telemetry/opentelemetry-specification/pull/4851">#4851</a>).
 *     Please use {@link W3CTraceContextPropagator} instead.
 */
@Immutable
@Deprecated
public final class OtTracePropagator implements TextMapPropagator {

  static final String TRACE_ID_HEADER = "ot-tracer-traceid";
  static final String SPAN_ID_HEADER = "ot-tracer-spanid";
  static final String SAMPLED_HEADER = "ot-tracer-sampled";
  static final String PREFIX_BAGGAGE_HEADER = "ot-baggage-";
  private static final Collection<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER));

  // No limits are defined by the OT trace format; borrow the W3C Baggage spec limits as a
  // defense-in-depth measure (https://www.w3.org/TR/baggage/#limits).
  private static final int MAX_BAGGAGE_ENTRIES = 64;
  private static final int MAX_BAGGAGE_BYTES = 8192;

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
    SpanContext spanContext = Span.fromContext(context).getSpanContext();
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
      int[] entriesEmitted = {0};
      int[] bytesEmitted = {0};
      baggage.forEach(
          (key, baggageEntry) -> {
            if (entriesEmitted[0] >= MAX_BAGGAGE_ENTRIES) {
              return;
            }
            String value = baggageEntry.getValue();
            if (bytesEmitted[0] + key.length() + value.length() > MAX_BAGGAGE_BYTES) {
              return;
            }
            setter.set(carrier, PREFIX_BAGGAGE_HEADER + key, value);
            entriesEmitted[0]++;
            bytesEmitted[0] += key.length() + value.length();
          });
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

    String incomingSpanId = getter.get(carrier, SPAN_ID_HEADER);
    String spanId =
        incomingSpanId == null
            ? SpanId.getInvalid()
            : StringUtils.padLeft(incomingSpanId, MAX_SPAN_ID_LENGTH);

    String sampled = getter.get(carrier, SAMPLED_HEADER);
    SpanContext spanContext = buildSpanContext(traceId, spanId, sampled);
    if (!spanContext.isValid()) {
      return context;
    }

    Context extractedContext = context.with(Span.wrap(spanContext));

    // Baggage is only extracted if there is a valid SpanContext
    if (carrier != null) {
      BaggageBuilder baggageBuilder = Baggage.builder();
      int entriesAdded = 0;
      int bytesAdded = 0;
      for (String key : getter.keys(carrier)) {
        if (entriesAdded >= MAX_BAGGAGE_ENTRIES || bytesAdded > MAX_BAGGAGE_BYTES) {
          break;
        }
        String lowercaseKey = key.toLowerCase(Locale.ROOT);
        if (!lowercaseKey.startsWith(PREFIX_BAGGAGE_HEADER)) {
          continue;
        }
        String value = getter.get(carrier, key);
        if (value == null) {
          continue;
        }
        String baggageKey =
            lowercaseKey.substring(OtTracePropagator.PREFIX_BAGGAGE_HEADER.length());
        if (bytesAdded + baggageKey.length() + value.length() > MAX_BAGGAGE_BYTES) {
          break;
        }
        baggageBuilder.put(baggageKey, value);
        entriesAdded++;
        bytesAdded += baggageKey.length() + value.length();
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

  @Override
  public String toString() {
    return "OtTracePropagator";
  }
}
