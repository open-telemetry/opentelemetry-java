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
import io.opentelemetry.sdk.metrics.state.InstrumentStorage;
import io.opentelemetry.sdk.metrics.state.InstrumentStorageRegistry;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
public class SdkMeter implements Meter {
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationLibraryInfo);
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return new LongSdkCounterBuilder(meterProviderSharedState, meterSharedState, name, "", "1");
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return new LongSdkUpDownCounterBuilder(
        meterProviderSharedState, meterSharedState, name, "", "1");
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return new DoubleSdkHistogramBuilder(meterProviderSharedState, meterSharedState, name, "", "1");
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return new DoubleSdkGaugeBuilder(meterProviderSharedState, meterSharedState, name, "", "1");
  }

  /** Collects all the metric recordings that changed since the previous call. */
  Collection<MetricData> collectAll(
      CollectionHandle collector, Set<CollectionHandle> allCollectors, long epochNanos) {
    InstrumentStorageRegistry instrumentRegistry = meterSharedState.getInstrumentStorageRegistry();
    Collection<InstrumentStorage> instruments = instrumentRegistry.getInstruments();
    List<MetricData> result = new ArrayList<>(instruments.size());
    for (InstrumentStorage instrument : instruments) {
      result.addAll(
          instrument.collectAndReset(
              collector, allCollectors, meterProviderSharedState.getStartEpochNanos(), epochNanos));
    }
    return result;
  }
}
