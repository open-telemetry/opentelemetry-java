/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * State for a {@code Meter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class MeterSharedState {

  private final Object collectLock = new Object();
  private final Object callbackLock = new Object();

  @GuardedBy("callbackLock")
  private final List<CallbackRegistration> callbackRegistrations = new ArrayList<>();

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final MetricStorageRegistry metricStorageRegistry;

  private MeterSharedState(
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricStorageRegistry metricStorageRegistry) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.metricStorageRegistry = metricStorageRegistry;
  }

  public static MeterSharedState create(InstrumentationScopeInfo instrumentationScopeInfo) {
    return new MeterSharedState(instrumentationScopeInfo, new MetricStorageRegistry());
  }

  /**
   * Unregister the callback.
   *
   * <p>Callbacks are originally registered via {@link #registerCallback(CallbackRegistration)}.
   */
  public void removeCallback(CallbackRegistration callbackRegistration) {
    synchronized (callbackLock) {
      this.callbackRegistrations.remove(callbackRegistration);
    }
  }

  /**
   * Register the callback.
   *
   * <p>The callback will be invoked once per collection until unregistered via {@link
   * #removeCallback(CallbackRegistration)}.
   */
  public final void registerCallback(CallbackRegistration callbackRegistration) {
    synchronized (callbackLock) {
      callbackRegistrations.add(callbackRegistration);
    }
  }

  // only visible for testing.
  /** Returns the {@link InstrumentationScopeInfo} for this {@code Meter}. */
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  /** Returns the metric storage for metrics in this {@code Meter}. */
  MetricStorageRegistry getMetricStorageRegistry() {
    return metricStorageRegistry;
  }

  /** Collects all accumulated metric stream points. */
  public List<MetricData> collectAll(
      CollectionInfo collectionInfo,
      MeterProviderSharedState meterProviderSharedState,
      long epochNanos,
      boolean suppressSynchronousCollection) {
    List<CallbackRegistration> currentRegisteredCallbacks;
    synchronized (callbackLock) {
      currentRegisteredCallbacks = new ArrayList<>(callbackRegistrations);
    }
    // Collections across all readers are sequential
    synchronized (collectLock) {
      for (CallbackRegistration callbackRegistration : currentRegisteredCallbacks) {
        callbackRegistration.invokeCallback();
      }

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

  /** Register new asynchronous storage associated with a given instrument. */
  public final SdkObservableMeasurement registerObservableMeasurement(
      InstrumentDescriptor instrumentDescriptor,
      MeterProviderSharedState meterProviderSharedState) {
    List<AsynchronousMetricStorage<?>> storages =
        meterProviderSharedState
            .getViewRegistry()
            .findViews(instrumentDescriptor, getInstrumentationScopeInfo())
            .stream()
            .map(view -> AsynchronousMetricStorage.create(view, instrumentDescriptor))
            .filter(storage -> !storage.isEmpty())
            .collect(toList());

    List<AsynchronousMetricStorage<?>> registeredStorages = new ArrayList<>(storages.size());
    for (AsynchronousMetricStorage<?> storage : storages) {
      registeredStorages.add(getMetricStorageRegistry().register(storage));
    }

    return SdkObservableMeasurement.create(
        instrumentationScopeInfo, instrumentDescriptor, registeredStorages);
  }
}
