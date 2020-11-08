/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.B3Propagator.DEBUG_HEADER;
import static io.opentelemetry.extension.trace.propagation.B3Propagator.SAMPLED_HEADER;
import static io.opentelemetry.extension.trace.propagation.B3Propagator.SPAN_ID_HEADER;
import static io.opentelemetry.extension.trace.propagation.B3Propagator.TRACE_ID_HEADER;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorExtractorMultipleHeaders implements B3PropagatorExtractor {
  private static final Logger logger =
      Logger.getLogger(B3PropagatorExtractorMultipleHeaders.class.getName());

  @Override
  public <C> Optional<Context> extract(
      Context context, C carrier, TextMapPropagator.Getter<C> getter) {
    Objects.requireNonNull(carrier, "carrier");
    Objects.requireNonNull(getter, "getter");
    return extractSpanContextFromMultipleHeaders(context, carrier, getter);
  }

  private static <C> Optional<Context> extractSpanContextFromMultipleHeaders(
      Context context, C carrier, TextMapPropagator.Getter<C> getter) {
    String traceId = getter.get(carrier, TRACE_ID_HEADER);
    if (StringUtils.isNullOrEmpty(traceId)) {
      return Optional.empty();
    }
    if (!Common.isTraceIdValid(traceId)) {
      logger.fine(
          "Invalid TraceId in B3 header: " + traceId + "'. Returning INVALID span context.");
      return Optional.empty();
    }

    String spanId = getter.get(carrier, SPAN_ID_HEADER);
    if (!Common.isSpanIdValid(spanId)) {
      logger.fine("Invalid SpanId in B3 header: " + spanId + "'. Returning INVALID span context.");
      return Optional.empty();
    }

    // if debug flag is set, then set sampled flag, and also store the B3 debug flag in the context
    // for onward use by B3 injector
    if (Common.TRUE_INT.equals(getter.get(carrier, DEBUG_HEADER))) {
      return Optional.of(
          context
              .with(B3Propagator.DEBUG_CONTEXT_KEY, Common.TRUE_INT)
              .with(Span.wrap(Common.buildSpanContext(traceId, spanId, Common.TRUE_INT))));
    }

    String sampled = getter.get(carrier, SAMPLED_HEADER);
    return Optional.of(context.with(Span.wrap(Common.buildSpanContext(traceId, spanId, sampled))));
  }
}
