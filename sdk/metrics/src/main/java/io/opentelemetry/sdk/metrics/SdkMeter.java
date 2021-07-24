/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationLibraryInfo);
  }

  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return meterSharedState.getInstrumentationLibraryInfo();
  }

  /** Collects all the metric recordings that changed since the previous call. */
  Collection<MetricData> collectAll(long epochNanos) {
    InstrumentRegistry instrumentRegistry = meterSharedState.getInstrumentRegistry();
    Collection<AbstractInstrument> instruments = instrumentRegistry.getInstruments();
    List<MetricData> result = new ArrayList<>(instruments.size());
    for (AbstractInstrument instrument : instruments) {
      result.addAll(instrument.collectAll(epochNanos));
    }
    return result;
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return new LongCounterSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return new LongUpDownCounterSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return new DoubleValueRecorderSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return new DoubleValueObserverSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }
}
