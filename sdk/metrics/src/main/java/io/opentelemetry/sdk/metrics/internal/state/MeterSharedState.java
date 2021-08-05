/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class MeterSharedState {
  public static MeterSharedState create(InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new AutoValue_MeterSharedState(instrumentationLibraryInfo, new MetricStorageRegistry());
  }

  // only visible for testing.
  public abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  abstract MetricStorageRegistry getMetricStorageRegistry();

  public List<MetricData> collectAll(
      MeterProviderSharedState meterProviderSharedState, long epochNanos) {
    Collection<MetricStorage> metrics = getMetricStorageRegistry().getMetrics();
    List<MetricData> result = new ArrayList<>(metrics.size());
    for (MetricStorage metric : metrics) {
      MetricData current =
          metric.collectAndReset(meterProviderSharedState.getStartEpochNanos(), epochNanos);
      if (current != null) {
        result.add(current);
      }
    }
    return result;
  }

  public final WriteableMetricStorage registerSynchronousMetricStorage(
      InstrumentDescriptor instrument, MeterProviderSharedState meterProviderSharedState) {
    return getMetricStorageRegistry()
        .register(SynchronousMetricStorage.create(meterProviderSharedState, this, instrument));
  }

  public final MetricStorage registerLongAsynchronousInstrument(
      InstrumentDescriptor instrument,
      MeterProviderSharedState meterProviderSharedState,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    return getMetricStorageRegistry()
        .register(
            AsynchronousMetricStorage.longAsynchronousAccumulator(
                meterProviderSharedState, this, instrument, metricUpdater));
  }

  public final MetricStorage registerDoubleAsynchronousInstrument(
      InstrumentDescriptor instrument,
      MeterProviderSharedState meterProviderSharedState,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {

    return getMetricStorageRegistry()
        .register(
            AsynchronousMetricStorage.doubleAsynchronousAccumulator(
                meterProviderSharedState, this, instrument, metricUpdater));
  }
}
