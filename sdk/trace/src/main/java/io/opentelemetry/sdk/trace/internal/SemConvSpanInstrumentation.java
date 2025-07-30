/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.function.Supplier;
import javax.annotation.Nullable;

class SemConvSpanInstrumentation implements SpanInstrumentation {

  private final Supplier<MeterProvider> meterProviderSupplier;

  private static final Attributes SAMPLING_DROP_ATTRIBUTES =
      Attributes.of(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT, "DROP");
  private static final Attributes SAMPLING_RECORD_ONLY_ATTRIBUTES =
      Attributes.of(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY");
  private static final Attributes SAMPLING_RECORD_AND_SAMPLED_ATTRIBUTES =
      Attributes.of(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE");

  @Nullable private volatile LongUpDownCounter live = null;
  @Nullable private volatile LongCounter started = null;

  SemConvSpanInstrumentation(Supplier<MeterProvider> meterProviderSupplier) {
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

  private LongCounter started() {
    LongCounter started = this.started;
    if (started == null) {
      started =
          meter()
              .counterBuilder("otel.sdk.span.started")
              .setUnit("{span}")
              .setDescription("The number of created spans")
              .build();
      this.started = started;
    }
    return started;
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

  // TODO: Add test verifying attribute values when released in semantic conventions
  static String getParentOriginAttributeValue(SpanContext parentSpanContext) {
    if (!parentSpanContext.isValid()) {
      return "none";
    } else if (parentSpanContext.isRemote()) {
      return "remote";
    } else {
      return "local";
    }
  }

  @Override
  public SpanInstrumentation.Recording recordSpanStart(
      SamplingResult samplingResult, SpanContext parentSpanContext) {
    Attributes samplingResultAttribs =
        getAttributesForSamplingDecisions(samplingResult.getDecision());
    Attributes startAttributes =
        samplingResultAttribs.toBuilder()
            .put(
                SemConvAttributes.OTEL_SPAN_PARENT_ORIGIN,
                getParentOriginAttributeValue(parentSpanContext))
            .build();
    started().add(1L, startAttributes);
    if (samplingResult.getDecision() == SamplingDecision.DROP) {
      // Per semconv, otel.sdk.span.live is NOT collected for non-recording spans
      return NoopSpanInstrumentation.RECORDING_INSTANCE;
    }
    live().add(1, samplingResultAttribs);
    return new Recording(samplingResultAttribs);
  }

  private class Recording implements SpanInstrumentation.Recording {

    private final Attributes attributes;

    @GuardedBy("this")
    private boolean endAlreadyReported = false;

    private Recording(Attributes attributes) {
      this.attributes = attributes;
    }

    @Override
    public boolean isNoop() {
      return false;
    }

    @Override
    public synchronized void recordSpanEnd() {
      if (endAlreadyReported) {
        return;
      }
      endAlreadyReported = true;
      live().add(-1, attributes);
    }
  }
}
