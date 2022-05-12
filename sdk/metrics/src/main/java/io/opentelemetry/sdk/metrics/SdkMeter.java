/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.internal.ValidationUtil;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.Collection;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {

  /**
   * Message appended to warnings when {@link ValidationUtil#checkValidInstrumentName(String,
   * String)} is {@code false}.
   */
  private static final String NOOP_INSTRUMENT_WARNING = " Returning noop instrument.";

  private static final Meter NOOP_METER = MeterProvider.noop().get("noop");
  private static final String NOOP_INSTRUMENT_NAME = "noop";

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationScopeInfo);
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  /** Collects all the metric recordings that changed since the previous call. */
  Collection<MetricData> collectAll(
      CollectionInfo collectionInfo, long epochNanos, boolean suppressSynchronousCollection) {
    return meterSharedState.collectAll(
        collectionInfo, meterProviderSharedState, epochNanos, suppressSynchronousCollection);
  }

  /** Reset the meter, clearing all registered instruments. */
  void resetForTest() {
    this.meterSharedState.resetForTest();
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return !ValidationUtil.checkValidInstrumentName(name, NOOP_INSTRUMENT_WARNING)
        ? NOOP_METER.counterBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkLongCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return !ValidationUtil.checkValidInstrumentName(name, NOOP_INSTRUMENT_WARNING)
        ? NOOP_METER.upDownCounterBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkLongUpDownCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return !ValidationUtil.checkValidInstrumentName(name, NOOP_INSTRUMENT_WARNING)
        ? NOOP_METER.histogramBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkDoubleHistogram.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return !ValidationUtil.checkValidInstrumentName(name, NOOP_INSTRUMENT_WARNING)
        ? NOOP_METER.gaugeBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkDoubleGaugeBuilder(meterProviderSharedState, meterSharedState, name);
  }
}
