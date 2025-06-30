/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class SemConvSpanInstrumentation implements SpanInstrumentation {

  private final Supplier<MeterProvider> meterProviderSupplier;

  private static final Attributes SAMPLING_DROP_ATTRIBUTES =
      Attributes.of(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT, "DROP");
  private static final Attributes SAMPLING_RECORD_ONLY_ATTRIBUTES =
      Attributes.of(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY");
  private static final Attributes SAMPLING_RECORD_AND_SAMPLED_ATTRIBUTES =
      Attributes.of(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE");

  @Nullable private volatile LongUpDownCounter live = null;
  @Nullable private volatile LongCounter ended = null;

  public SemConvSpanInstrumentation(Supplier<MeterProvider> meterProviderSupplier) {
    this.meterProviderSupplier = meterProviderSupplier;
  }

  private Meter meter() {
    MeterProvider meterProvider = meterProviderSupplier.get();
    if (meterProvider == null) {
      meterProvider = MeterProvider.noop();
    }
    return meterProvider.get("io.opentelemetry.sdk.trace");
  }

  private LongUpDownCounter live() {
    LongUpDownCounter live = this.live;
    if (live == null) {
      live =
          meter()
              .upDownCounterBuilder("otel.sdk.span.live")
              .setUnit("{span}")
              .setDescription(
                  "The number of created spans for which the end operation has not been called yet")
              .build();
      this.live = live;
    }
    return live;
  }

  private LongCounter ended() {
    LongCounter ended = this.ended;
    if (ended == null) {
      ended =
          meter()
              .counterBuilder("otel.sdk.span.ended")
              .setUnit("{span}")
              .setDescription("The number of created spans for which the end operation was called")
              .build();
      this.ended = ended;
    }
    return ended;
  }

  static Attributes getAttributesForSamplingDecisions(SamplingDecision decision) {
    switch (decision) {
      case DROP:
        return SAMPLING_DROP_ATTRIBUTES;
      case RECORD_ONLY:
        return SAMPLING_RECORD_ONLY_ATTRIBUTES;
      case RECORD_AND_SAMPLE:
        return SAMPLING_RECORD_AND_SAMPLED_ATTRIBUTES;
    }
    throw new IllegalStateException("Unhandled SamplingDecision case: " + decision);
  }

  @Override
  public SpanInstrumentation.Recording recordSpanStart(SamplingResult samplingResult) {
    Attributes attribs = getAttributesForSamplingDecisions(samplingResult.getDecision());
    live().add(1, attribs);
    return new Recording(attribs);
  }

  private class Recording implements SpanInstrumentation.Recording {

    private final Attributes attributes;
    private boolean endAlreadyReported = false;

    private Recording(Attributes attributes) {
      this.attributes = attributes;
    }

    @Override
    public synchronized void recordSpanEnd() {
      if (endAlreadyReported) {
        return;
      }
      endAlreadyReported = true;
      live().add(-1, attributes);
      ended().add(1, attributes);
    }
  }
}
