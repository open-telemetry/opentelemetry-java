/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {

  private static final Logger logger = Logger.getLogger(SdkMeter.class.getName());

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

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return new SdkLongCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return new SdkLongUpDownCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return new SdkDoubleHistogram.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return new SdkDoubleGaugeBuilder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public BatchCallback batchCallback(
      Runnable callback,
      ObservableMeasurement observableMeasurement,
      ObservableMeasurement... observableMeasurements) {
    Set<ObservableMeasurement> measurements = new HashSet<>();
    measurements.add(observableMeasurement);
    Collections.addAll(measurements, observableMeasurements);

    List<SdkObservableMeasurement> sdkMeasurements = new ArrayList<>();
    for (ObservableMeasurement measurement : measurements) {
      if (!(measurement instanceof SdkObservableMeasurement)) {
        logger.log(
            Level.WARNING,
            "batchCallback called with instruments that were not created by the SDK.");
        continue;
      }
      SdkObservableMeasurement sdkMeasurement = (SdkObservableMeasurement) measurement;
      if (!meterSharedState
          .getInstrumentationScopeInfo()
          .equals(sdkMeasurement.getInstrumentationScopeInfo())) {
        logger.log(
            Level.WARNING,
            "batchCallback called with instruments that belong to a different Meter.");
        continue;
      }
      sdkMeasurements.add(sdkMeasurement);
    }

    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(sdkMeasurements, callback);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }
}
