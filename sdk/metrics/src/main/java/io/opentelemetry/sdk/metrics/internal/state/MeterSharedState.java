/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static java.util.stream.Collectors.toList;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
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

  public static MeterSharedState create(InstrumentationScopeInfo instrumentationScopeInfo) {
    return new AutoValue_MeterSharedState(instrumentationScopeInfo, new MetricStorageRegistry());
  }

  MeterSharedState() {}

  // only visible for testing.
  /** Returns the {@link InstrumentationScopeInfo} for this {@code Meter}. */
  public abstract InstrumentationScopeInfo getInstrumentationScopeInfo();

  /** Returns the metric storage for metrics in this {@code Meter}. */
  abstract MetricStorageRegistry getMetricStorageRegistry();

  /** Collects all accumulated metric stream points. */
  public List<MetricData> collectAll(
      CollectionInfo collectionInfo,
      MeterProviderSharedState meterProviderSharedState,
      long epochNanos,
      boolean suppressSynchronousCollection) {
    Collection<MetricStorage> metrics = getMetricStorageRegistry().getMetrics();
    List<MetricData> result = new ArrayList<>(metrics.size());
    for (MetricStorage metric : metrics) {
      MetricData current =
          metric.collectAndReset(
              collectionInfo,
              meterProviderSharedState.getResource(),
              getInstrumentationScopeInfo(),
              meterProviderSharedState.getStartEpochNanos(),
              epochNanos,
              suppressSynchronousCollection);
      // Ignore if the metric data doesn't have any data points, for example when aggregation is
      // Aggregation#drop()
      if (!current.isEmpty()) {
        result.add(current);
      }
    }
    return result;
  }

  /** Registers new synchronous storage associated with a given instrument. */
  public final WriteableMetricStorage registerSynchronousMetricStorage(
      InstrumentDescriptor instrument, MeterProviderSharedState meterProviderSharedState) {

    List<SynchronousMetricStorage> storages =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationScopeInfo())
            .stream()
            .map(
                view ->
                    SynchronousMetricStorage.create(
                        view, instrument, meterProviderSharedState.getExemplarFilter()))
            .filter(m -> !m.isEmpty())
            .collect(toList());

    List<SynchronousMetricStorage> registeredStorages = new ArrayList<>(storages.size());
    for (SynchronousMetricStorage storage : storages) {
      registeredStorages.add(getMetricStorageRegistry().register(storage));
    }

    if (registeredStorages.size() == 1) {
      return registeredStorages.get(0);
    }
    return new MultiWritableMetricStorage(registeredStorages);
  }

  /** Registers new asynchronous storage associated with a given {@code long} instrument. */
  public final List<AsynchronousMetricStorage<?, ObservableLongMeasurement>>
      registerLongAsynchronousInstrument(
          InstrumentDescriptor instrument,
          MeterProviderSharedState meterProviderSharedState,
          Consumer<ObservableLongMeasurement> callback) {

    List<AsynchronousMetricStorage<?, ObservableLongMeasurement>> storages =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationScopeInfo())
            .stream()
            .map(view -> AsynchronousMetricStorage.createLongAsyncStorage(view, instrument))
            .filter(storage -> !storage.isEmpty())
            .collect(toList());

    List<AsynchronousMetricStorage<?, ObservableLongMeasurement>> registeredStorages =
        new ArrayList<>();
    for (AsynchronousMetricStorage<?, ObservableLongMeasurement> storage : storages) {
      AsynchronousMetricStorage<?, ObservableLongMeasurement> registeredStorage =
          getMetricStorageRegistry().register(storage);
      registeredStorage.addCallback(callback);
      registeredStorages.add(registeredStorage);
    }
    return registeredStorages;
  }

  /** Registers new asynchronous storage associated with a given {@code double} instrument. */
  public final List<AsynchronousMetricStorage<?, ObservableDoubleMeasurement>>
      registerDoubleAsynchronousInstrument(
          InstrumentDescriptor instrument,
          MeterProviderSharedState meterProviderSharedState,
          Consumer<ObservableDoubleMeasurement> callback) {

    List<AsynchronousMetricStorage<?, ObservableDoubleMeasurement>> storages =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationScopeInfo())
            .stream()
            .map(view -> AsynchronousMetricStorage.createDoubleAsyncStorage(view, instrument))
            .filter(storage -> !storage.isEmpty())
            .collect(toList());

    List<AsynchronousMetricStorage<?, ObservableDoubleMeasurement>> registeredStorages =
        new ArrayList<>();
    for (AsynchronousMetricStorage<?, ObservableDoubleMeasurement> storage : storages) {
      AsynchronousMetricStorage<?, ObservableDoubleMeasurement> registeredStorage =
          getMetricStorageRegistry().register(storage);
      registeredStorage.addCallback(callback);
      registeredStorages.add(registeredStorage);
    }
    return registeredStorages;
  }
}
