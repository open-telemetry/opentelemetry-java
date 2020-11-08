/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.B3Propagator.COMBINED_HEADER;
import static io.opentelemetry.extension.trace.propagation.B3Propagator.COMBINED_HEADER_DELIMITER;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorExtractorSingleHeader implements B3PropagatorExtractor {
  private static final Logger logger =
      Logger.getLogger(B3PropagatorExtractorSingleHeader.class.getName());

  @Override
  public <C> Optional<Context> extract(
      Context context, C carrier, TextMapPropagator.Getter<C> getter) {
    Objects.requireNonNull(carrier, "carrier");
    Objects.requireNonNull(getter, "getter");
    return extractSpanContextFromSingleHeader(context, carrier, getter);
  }

  @SuppressWarnings("StringSplitter")
  private static <C> Optional<Context> extractSpanContextFromSingleHeader(
      Context context, C carrier, TextMapPropagator.Getter<C> getter) {
    String value = getter.get(carrier, COMBINED_HEADER);
    if (StringUtils.isNullOrEmpty(value)) {
      return Optional.empty();
    }

    // must have between 2 and 4 hyphen delimited parts:
    //   traceId-spanId-sampled-parentSpanId (last two are optional)
    // NOTE: we do not use parentSpanId
    String[] parts = value.split(COMBINED_HEADER_DELIMITER);
    if (parts.length < 2 || parts.length > 4) {
      logger.fine(
          "Invalid combined header '" + COMBINED_HEADER + ". Returning INVALID span context.");
      return Optional.empty();
    }

    String traceId = parts[0];
    if (!Common.isTraceIdValid(traceId)) {
      logger.fine(
          "Invalid TraceId in B3 header: " + COMBINED_HEADER + ". Returning INVALID span context.");
      return Optional.empty();
    }

    String spanId = parts[1];
    if (!Common.isSpanIdValid(spanId)) {
      logger.fine(
          "Invalid SpanId in B3 header: " + COMBINED_HEADER + ". Returning INVALID span context.");
      return Optional.empty();
    }

    String sampled = parts.length >= 3 ? parts[2] : null;

    // if sampled is marked as 'd'ebug, then set sampled flag, and also store the B3 debug flag in
    // the context for onward use by the B3 injector
    if (sampled != null && sampled.equals("d")) {
      return Optional.of(
          context
              .with(B3Propagator.DEBUG_CONTEXT_KEY, Common.TRUE_INT)
              .with(Span.wrap(Common.buildSpanContext(traceId, spanId, Common.TRUE_INT))));
    }

    return Optional.of(context.with(Span.wrap(Common.buildSpanContext(traceId, spanId, sampled))));
  }
}
