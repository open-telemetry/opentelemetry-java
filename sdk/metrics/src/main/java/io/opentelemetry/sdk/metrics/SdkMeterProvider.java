/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * {@code SdkMeterProvider} implementation for {@link MeterProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}.
 *
 * <p>WARNING: A MetricProducer is stateful. It will only return changes since the last time it was
 * accessed. This means that if more than one {@link
 * io.opentelemetry.sdk.metrics.export.MetricExporter} has a handle to this MetricProducer, the two
 * exporters will not receive copies of the same metric data to export.
 */
public final class SdkMeterProvider implements MeterProvider {

  private static final Logger LOGGER = Logger.getLogger(SdkMeterProvider.class.getName());
  static final String DEFAULT_METER_NAME = "unknown";
  private final ComponentRegistry<SdkMeter> registry;
  private final MeterProviderSharedState sharedState;

  private final ReentrantLock collectorsLock = new ReentrantLock();

  @GuardedBy("collectorsLock")
  private final Set<CollectionHandle> collectors = CollectionHandle.mutableSet();

  @GuardedBy("collectorsLock")
  private final List<MetricReader> readers = new ArrayList<>();

  SdkMeterProvider(Clock clock, Resource resource, ViewRegistry viewRegistry) {
    this.sharedState = MeterProviderSharedState.create(clock, resource, viewRegistry);
    this.registry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new SdkMeter(sharedState, instrumentationLibraryInfo));
  }

  @Override
  public MeterBuilder meterBuilder(@Nullable String instrumentationName) {
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      LOGGER.fine("Meter requested without instrumentation name.");
      instrumentationName = DEFAULT_METER_NAME;
    }
    return new SdkMeterBuilder(registry, instrumentationName);
  }

  /**
   * Registers a new metric reader on this meter provider.
   *
   * @param <R> the type of the reader.
   * @param factory a constructor for the reader, given access to SDK internals.
   * @return the registered reader.
   */
  <R extends MetricReader> R register(MetricReader.Factory<R> factory) {
    collectorsLock.lock();
    try {
      CollectionHandle handle = CollectionHandle.create();
      collectors.add(handle);
      R reader = factory.apply(new LeasedMetricProducer(handle));
      readers.add(reader);
      return reader;
    } finally {
      collectorsLock.unlock();
    }
  }

  /** Forces metric readers to immediately read metrics, if able. */
  public CompletableResultCode flush() {
    collectorsLock.lock();
    try {
      List<CompletableResultCode> results = new ArrayList<>();
      for (MetricReader reader : readers) {
        results.add(reader.flush());
      }
      return CompletableResultCode.ofAll(results);
    } finally {
      collectorsLock.unlock();
    }
  }

  /** Shuts down metric collection and all associated metric readers. */
  public CompletableResultCode shutdown() {
    collectorsLock.lock();
    try {
      List<CompletableResultCode> results = new ArrayList<>();
      for (MetricReader reader : readers) {
        results.add(reader.shutdown());
      }
      readers.clear();
      collectors.clear();
      return CompletableResultCode.ofAll(results);
    } finally {
      collectorsLock.unlock();
    }
  }

  /**
   * Returns a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   *
   * @return a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   */
  public static SdkMeterProviderBuilder builder() {
    return new SdkMeterProviderBuilder();
  }

  /** Helper class to expose registered metric exports. */
  private class LeasedMetricProducer implements MetricProducer {
    private final CollectionHandle handle;

    LeasedMetricProducer(CollectionHandle handle) {
      this.handle = handle;
    }

    @Override
    public Collection<MetricData> collectAllMetrics() {
      Collection<SdkMeter> meters = registry.getComponents();
      List<MetricData> result = new ArrayList<>(meters.size());
      Set<CollectionHandle> allCollectors;
      collectorsLock.lock();
      try {
        allCollectors = collectors;
      } finally {
        collectorsLock.unlock();
      }
      for (SdkMeter meter : meters) {
        result.addAll(meter.collectAll(handle, allCollectors, sharedState.getClock().now()));
      }
      return Collections.unmodifiableCollection(result);
    }
  }
}
