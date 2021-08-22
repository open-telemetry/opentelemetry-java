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
import io.opentelemetry.sdk.metrics.view.View;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.concurrent.Immutable;

/**
 * State for a {@code Meter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MeterSharedState {
  public static MeterSharedState create(InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new AutoValue_MeterSharedState(instrumentationLibraryInfo, new MetricStorageRegistry());
  }

  // only visible for testing.
  /** Returns the {@link InstrumentationLibraryInfo} for this {@code Meter}. */
  public abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /** Returns the metric storage for metrics in this {@code Meter}. */
  abstract MetricStorageRegistry getMetricStorageRegistry();

  /** Collects all accumulated metric stream points. */
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

  /** Registers new synchronous storage associated with a given instrument. */
  public final WriteableMetricStorage registerSynchronousMetricStorage(
      InstrumentDescriptor instrument, MeterProviderSharedState meterProviderSharedState) {
    // TODO - we  need to iterate over all possible views here and register each we find.
    List<View> views =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationLibraryInfo());

    if (views.size() == 1) {
      return getMetricStorageRegistry()
          .register(
              SynchronousMetricStorage.create(
                  views.get(0),
                  instrument,
                  meterProviderSharedState.getResource(),
                  getInstrumentationLibraryInfo(),
                  meterProviderSharedState.getStartEpochNanos()));
    }
    List<WriteableMetricStorage> storage = new ArrayList<>();
    for (View view : views) {
      storage.add(
          getMetricStorageRegistry()
              .register(
                  SynchronousMetricStorage.create(
                      view,
                      instrument,
                      meterProviderSharedState.getResource(),
                      getInstrumentationLibraryInfo(),
                      meterProviderSharedState.getStartEpochNanos())));
    }
    return new MultiWritableMetricStorage(storage);
  }

  /** Registers new asynchronous storage associated with a given {@code long} instrument. */
  public final void registerLongAsynchronousInstrument(
      InstrumentDescriptor instrument,
      MeterProviderSharedState meterProviderSharedState,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    // TODO - we should avoid registering independent storage that calls observables over and over.
    List<View> views =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationLibraryInfo());
    for (View view : views) {
      getMetricStorageRegistry()
          .register(
              AsynchronousMetricStorage.longAsynchronousAccumulator(
                  view,
                  instrument,
                  meterProviderSharedState.getResource(),
                  getInstrumentationLibraryInfo(),
                  meterProviderSharedState.getStartEpochNanos(),
                  metricUpdater));
    }
  }

  /** Registers new asynchronous storage associated with a given {@code double} instrument. */
  public final void registerDoubleAsynchronousInstrument(
      InstrumentDescriptor instrument,
      MeterProviderSharedState meterProviderSharedState,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    // TODO - we should avoid registering independent storage that calls observables over and over.
    List<View> views =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationLibraryInfo());
    for (View view : views) {
      getMetricStorageRegistry()
          .register(
              AsynchronousMetricStorage.doubleAsynchronousAccumulator(
                  view,
                  instrument,
                  meterProviderSharedState.getResource(),
                  getInstrumentationLibraryInfo(),
                  meterProviderSharedState.getStartEpochNanos(),
                  metricUpdater));
    }
  }
}
