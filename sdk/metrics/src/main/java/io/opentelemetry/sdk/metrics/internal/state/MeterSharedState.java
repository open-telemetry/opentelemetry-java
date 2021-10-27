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
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
      if (current != null) {
        result.add(current);
      }
    }
    return result;
  }

  /** Registers new synchronous storage associated with a given instrument. */
  public final WriteableMetricStorage registerSynchronousMetricStorage(
      InstrumentDescriptor instrument, MeterProviderSharedState meterProviderSharedState) {
    List<View> views =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrument, getInstrumentationLibraryInfo());
    List<WriteableMetricStorage> storage = new ArrayList<>(views.size());
    for (View view : views) {
      SynchronousMetricStorage currentStorage =
          SynchronousMetricStorage.create(
              view,
              instrument,
              meterProviderSharedState.getResource(),
              getInstrumentationLibraryInfo(),
              meterProviderSharedState.getExemplarFilter());
      // TODO - move this in a better location.
      if (SynchronousMetricStorage.empty().equals(currentStorage)) {
        continue;
      }
      try {
        storage.add(getMetricStorageRegistry().register(currentStorage));
      } catch (DuplicateMetricStorageException e) {
        logger.log(Level.WARNING, e, () -> "Failed to register metric.");
      }
    }
    if (storage.size() == 1) {
      return storage.get(0);
    }
    // If the size is 0, we return an, effectively, no-op writer.
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
      MetricStorage currentStorage =
          AsynchronousMetricStorage.longAsynchronousAccumulator(
              view,
              instrument,
              meterProviderSharedState.getResource(),
              getInstrumentationLibraryInfo(),
              metricUpdater);
      // TODO - move this in a better location.
      if (AsynchronousMetricStorage.empty().equals(currentStorage)) {
        continue;
      }
      try {
        getMetricStorageRegistry().register(currentStorage);
      } catch (DuplicateMetricStorageException e) {
        logger.log(Level.WARNING, e, () -> "Failed to register metric.");
      }
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
      MetricStorage currentStorage =
          AsynchronousMetricStorage.doubleAsynchronousAccumulator(
              view,
              instrument,
              meterProviderSharedState.getResource(),
              getInstrumentationLibraryInfo(),
              metricUpdater);
      // TODO - move this in a better location.
      if (AsynchronousMetricStorage.empty() == currentStorage) {
        continue;
      }
      try {
        getMetricStorageRegistry().register(currentStorage);
      } catch (DuplicateMetricStorageException e) {
        logger.log(Level.WARNING, e, () -> "Failed to register metric.");
      }
    }
  }
}
