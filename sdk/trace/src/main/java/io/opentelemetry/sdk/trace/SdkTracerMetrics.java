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
import java.util.function.Supplier;
import javax.annotation.Nullable;

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

  private static final Attributes noParentDrop =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN, "none", OTEL_SPAN_SAMPLING_RESULT, SamplingDecision.DROP.name());
  private static final Attributes noParentRecordOnly =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "none",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.RECORD_ONLY.name());
  private static final Attributes noParentRecordAndSample =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "none",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.RECORD_AND_SAMPLE.name());

  private static final Attributes remoteParentDrop =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "remote",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.DROP.name());
  private static final Attributes remoteParentRecordOnly =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "remote",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.RECORD_ONLY.name());
  private static final Attributes remoteParentRecordAndSample =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "remote",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.RECORD_AND_SAMPLE.name());

  private static final Attributes localParentDrop =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "local",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.DROP.name());
  private static final Attributes localParentRecordOnly =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "local",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.RECORD_ONLY.name());
  private static final Attributes localParentRecordAndSample =
      Attributes.of(
          OTEL_SPAN_PARENT_ORIGIN,
          "local",
          OTEL_SPAN_SAMPLING_RESULT,
          SamplingDecision.RECORD_AND_SAMPLE.name());

  private static final Attributes recordOnly =
      Attributes.of(OTEL_SPAN_SAMPLING_RESULT, SamplingDecision.RECORD_ONLY.name());
  private static final Attributes recordAndSample =
      Attributes.of(OTEL_SPAN_SAMPLING_RESULT, SamplingDecision.RECORD_AND_SAMPLE.name());

  private final Object lock = new Object();

  private final Supplier<MeterProvider> meterProvider;

  @Nullable private Meter meter;
  @Nullable private volatile LongCounter startedSpans;
  @Nullable private volatile LongUpDownCounter liveSpans;

  SdkTracerMetrics(Supplier<MeterProvider> meterProvider) {
    this.meterProvider = meterProvider;
  }

  /**
   * Records metrics for when a span starts and returns a {@link Runnable} to execute when ending
   * the span.
   */
  Runnable startSpan(SpanContext parentSpanContext, SamplingDecision samplingDecision) {
    if (!parentSpanContext.isValid()) {
      switch (samplingDecision) {
        case DROP:
          startedSpans().add(1, noParentDrop);
          return SdkTracerMetrics::noop;
        case RECORD_ONLY:
          startedSpans().add(1, noParentRecordOnly);
          liveSpans().add(1, recordOnly);
          return this::decrementRecordOnly;
        case RECORD_AND_SAMPLE:
          startedSpans().add(1, noParentRecordAndSample);
          liveSpans().add(1, recordAndSample);
          return this::decrementRecordAndSample;
      }
      throw new IllegalArgumentException("Unrecognized sampling decision: " + samplingDecision);
    } else if (parentSpanContext.isRemote()) {
      switch (samplingDecision) {
        case DROP:
          startedSpans().add(1, remoteParentDrop);
          return SdkTracerMetrics::noop;
        case RECORD_ONLY:
          startedSpans().add(1, remoteParentRecordOnly);
          liveSpans().add(1, recordOnly);
          return this::decrementRecordOnly;
        case RECORD_AND_SAMPLE:
          startedSpans().add(1, remoteParentRecordAndSample);
          liveSpans().add(1, recordAndSample);
          return this::decrementRecordAndSample;
      }
      throw new IllegalArgumentException("Unrecognized sampling decision: " + samplingDecision);
    }
    // local parent
    switch (samplingDecision) {
      case DROP:
        startedSpans().add(1, localParentDrop);
        return SdkTracerMetrics::noop;
      case RECORD_ONLY:
        startedSpans().add(1, localParentRecordOnly);
        liveSpans().add(1, recordOnly);
        return this::decrementRecordOnly;
      case RECORD_AND_SAMPLE:
        startedSpans().add(1, localParentRecordAndSample);
        liveSpans().add(1, recordAndSample);
        return this::decrementRecordAndSample;
    }
    throw new IllegalArgumentException("Unrecognized sampling decision: " + samplingDecision);
  }

  private static void noop() {}

  private void decrementRecordOnly() {
    liveSpans().add(-1, recordOnly);
  }

  private void decrementRecordAndSample() {
    liveSpans().add(-1, recordAndSample);
  }

  private LongCounter startedSpans() {
    LongCounter startedSpans = this.startedSpans;
    if (startedSpans == null) {
      synchronized (lock) {
        startedSpans = this.startedSpans;
        if (startedSpans == null) {
          startedSpans =
              meter()
                  .counterBuilder("otel.sdk.span.started")
                  .setUnit("{span}")
                  .setDescription("The number of created spans.")
                  .build();
          this.startedSpans = startedSpans;
        }
      }
    }
    return startedSpans;
  }

  private LongUpDownCounter liveSpans() {
    LongUpDownCounter liveSpans = this.liveSpans;
    if (liveSpans == null) {
      synchronized (lock) {
        liveSpans = this.liveSpans;
        if (liveSpans == null) {
          liveSpans =
              meter()
                  .upDownCounterBuilder("otel.sdk.span.live")
                  .setUnit("{span}")
                  .setDescription(
                      "The number of created spans with recording=true for which the end operation has not been called yet.")
                  .build();
          this.liveSpans = liveSpans;
        }
      }
    }
    return liveSpans;
  }

  private Meter meter() {
    if (meter == null) {
      // Safe to call from multiple threads.
      meter = meterProvider.get().get("io.opentelemetry.sdk.trace");
    }
    return meter;
  }
}
