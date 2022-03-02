/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static java.util.stream.Collectors.toList;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
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

  private static final Logger logger = Logger.getLogger(MeterSharedState.class.getName());

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
              getInstrumentationLibraryInfo(),
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

    List<WriteableMetricStorage> storage =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationLibraryInfo())
            .stream()
            .map(
                view ->
                    SynchronousMetricStorage.create(
                        view, instrument, meterProviderSharedState.getExemplarFilter()))
            .filter(m -> !m.isEmpty())
            .map(this::register)
            .filter(Objects::nonNull)
            .collect(toList());

    if (storage.size() == 1) {
      return storage.get(0);
    }
    // If the size is 0, we return an, effectively, no-op writer.
    return new MultiWritableMetricStorage(storage);
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
            .findViews(instrument, getInstrumentationLibraryInfo())
            .stream()
            .map(view -> AsynchronousMetricStorage.createLongAsyncStorage(view, instrument))
            .filter(storage -> !storage.isEmpty())
            .collect(toList());

    List<AsynchronousMetricStorage<?, ObservableLongMeasurement>> registeredStorages =
        new ArrayList<>();
    for (AsynchronousMetricStorage<?, ObservableLongMeasurement> storage : storages) {
      AsynchronousMetricStorage<?, ObservableLongMeasurement> registeredStorage = register(storage);
      if (registeredStorage != null) {
        registeredStorage.addCallback(callback);
        registeredStorages.add(registeredStorage);
      }
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
            .findViews(instrument, getInstrumentationLibraryInfo())
            .stream()
            .map(view -> AsynchronousMetricStorage.createDoubleAsyncStorage(view, instrument))
            .filter(storage -> !storage.isEmpty())
            .collect(toList());

    List<AsynchronousMetricStorage<?, ObservableDoubleMeasurement>> registeredStorages =
        new ArrayList<>();
    for (AsynchronousMetricStorage<?, ObservableDoubleMeasurement> storage : storages) {
      AsynchronousMetricStorage<?, ObservableDoubleMeasurement> registeredStorage =
          register(storage);
      if (registeredStorage != null) {
        registeredStorage.addCallback(callback);
        registeredStorages.add(registeredStorage);
      }
    }
    return registeredStorages;
  }

  @Nullable
  private <S extends MetricStorage> S register(S storage) {
    try {
      return getMetricStorageRegistry().register(storage);
    } catch (DuplicateMetricStorageException e) {
      if (logger.isLoggable(Level.WARNING)) {
        logger.log(Level.WARNING, DebugUtils.duplicateMetricErrorMessage(e), e);
      }
    }
    return null;
  }
}
