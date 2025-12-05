/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;

/**
 * SDK metrics exported for started and ended spans as defined in the <a
 * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#span-metrics">semantic
 * conventions</a>.
 */
final class SdkTracerMetrics {

  // Visible for testing
  static final AttributeKey<String> OTEL_SPAN_PARENT_ORIGIN = stringKey("otel.span.parent.origin");
  // Visible for testing
  static final AttributeKey<String> OTEL_SPAN_SAMPLING_RESULT =
      stringKey("otel.span.sampling_result");

  private final LongCounter startedSpans;
  private final LongUpDownCounter liveSpans;

  SdkTracerMetrics(MeterProvider meterProvider) {
    Meter meter = meterProvider.get("io.opentelemetry.sdk.trace");

    startedSpans =
        meter
            .counterBuilder("otel.sdk.span.started")
            .setUnit("{span}")
            .setDescription("The number of created spans.")
            .build();
    liveSpans =
        meter
            .upDownCounterBuilder("otel.sdk.span.live")
            .setUnit("{span}")
            .setDescription(
                "The number of created spans with recording=true for which the end operation has not been called yet.")
            .build();
  }

  /**
   * Records metrics for when a span starts and returns a {@link Runnable} to execute when ending
   * the span.
   */
  Runnable startSpan(SpanContext parentSpanContext, SamplingDecision samplingDecision) {
    startedSpans.add(
        1,
        Attributes.of(
            OTEL_SPAN_PARENT_ORIGIN,
            parentOrigin(parentSpanContext),
            OTEL_SPAN_SAMPLING_RESULT,
            samplingDecision.name()));

    if (samplingDecision == SamplingDecision.DROP) {
      return () -> {};
    }

    Attributes liveSpansAttributes =
        Attributes.of(OTEL_SPAN_SAMPLING_RESULT, samplingDecision.name());
    liveSpans.add(1, liveSpansAttributes);
    return () -> liveSpans.add(-1, liveSpansAttributes);
  }

  private static String parentOrigin(SpanContext parentSpanContext) {
    if (!parentSpanContext.isValid()) {
      return "none";
    }
    if (parentSpanContext.isRemote()) {
      return "remote";
    }
    return "local";
  }
}
