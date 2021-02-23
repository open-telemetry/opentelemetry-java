/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorExtractorMultipleHeaders implements B3PropagatorExtractor {
  private static final Logger logger =
      Logger.getLogger(B3PropagatorExtractorMultipleHeaders.class.getName());

  @SuppressWarnings("ConstantConditions")
  @Override
  public <C> Optional<Context> extract(Context context, C carrier, TextMapGetter<C> getter) {
    if (context == null || getter == null) {
      return Optional.empty();
    }
    return extractSpanContextFromMultipleHeaders(context, carrier, getter);
  }

  private static <C> Optional<Context> extractSpanContextFromMultipleHeaders(
      Context context, C carrier, TextMapGetter<C> getter) {
    String traceId = getter.get(carrier, B3Propagator.TRACE_ID_HEADER);
    if (!Common.isTraceIdValid(traceId)) {
      logger.fine(
          "Invalid TraceId in B3 header: " + traceId + "'. Returning INVALID span context.");
      return Optional.empty();
    }

    String spanId = getter.get(carrier, B3Propagator.SPAN_ID_HEADER);
    if (!Common.isSpanIdValid(spanId)) {
      logger.fine("Invalid SpanId in B3 header: " + spanId + "'. Returning INVALID span context.");
      return Optional.empty();
    }

    // if debug flag is set, then set sampled flag, and also set B3 debug to true in the context
    // for onward use by B3 injector
    if (B3Propagator.MULTI_HEADER_DEBUG.equals(getter.get(carrier, B3Propagator.DEBUG_HEADER))) {
      return Optional.of(
          context
              .with(B3Propagator.DEBUG_CONTEXT_KEY, true)
              .with(Span.wrap(Common.buildSpanContext(traceId, spanId, Common.TRUE_INT))));
    }

    String sampled = getter.get(carrier, B3Propagator.SAMPLED_HEADER);
    return Optional.of(context.with(Span.wrap(Common.buildSpanContext(traceId, spanId, sampled))));
  }
}
