/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** SDK implementation for {@link MeterProvider}. */
public final class SdkMeterProvider implements MeterProvider, Closeable {

  private static final Logger LOGGER = Logger.getLogger(SdkMeterProvider.class.getName());
  static final String DEFAULT_METER_NAME = "unknown";

  private final ComponentRegistry<SdkMeter> registry;
  private final MeterProviderSharedState sharedState;
  private final Map<CollectionHandle, CollectionInfo> collectionInfoMap;
  private final AtomicBoolean isClosed = new AtomicBoolean(false);
  private final AtomicLong lastCollectionTimestamp;

  // Minimum amount of time we allow between synchronous collections.
  // This meant to reduce overhead when multiple exporters attempt to read metrics quickly.
  // TODO: This should be configurable at the SDK level.
  private static final long MINIMUM_COLLECTION_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

  /**
   * Returns a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   *
   * @return a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   */
  public static SdkMeterProviderBuilder builder() {
    return new SdkMeterProviderBuilder();
  }

  SdkMeterProvider(
      List<MetricReaderFactory> readerFactories,
      Clock clock,
      Resource resource,
      ViewRegistry viewRegistry,
      ExemplarFilter exemplarSampler) {
    this.sharedState =
        MeterProviderSharedState.create(clock, resource, viewRegistry, exemplarSampler);
    this.registry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new SdkMeter(sharedState, instrumentationLibraryInfo));
    this.lastCollectionTimestamp =
        new AtomicLong(clock.nanoTime() - MINIMUM_COLLECTION_INTERVAL_NANOS);

    // Here we construct our own unique handle ids for this SDK.
    // These are guaranteed to be unique per-reader for this SDK, and only this SDK.
    // These are *only* mutated in our constructor, and safe to use concurrently after construction.
    Set<CollectionHandle> collectors = CollectionHandle.mutableSet();
    collectionInfoMap = new HashMap<>();
    Supplier<CollectionHandle> handleSupplier = CollectionHandle.createSupplier();
    for (MetricReaderFactory readerFactory : readerFactories) {
      CollectionHandle handle = handleSupplier.get();
      // TODO: handle failure in creation or just crash?
      MetricReader reader = readerFactory.apply(new LeasedMetricProducer(handle));
      collectionInfoMap.put(handle, CollectionInfo.create(handle, collectors, reader));
      collectors.add(handle);
    }
  }

  @Override
  public MeterBuilder meterBuilder(String instrumentationName) {
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      LOGGER.fine("Meter requested without instrumentation name.");
      instrumentationName = DEFAULT_METER_NAME;
    }
    return new SdkMeterBuilder(registry, instrumentationName);
  }

  /**
   * Call {@link MetricReader#flush()} on all metric readers associated with this provider. The
   * resulting {@link CompletableResultCode} completes when all complete.
   */
  public CompletableResultCode forceFlush() {
    List<CompletableResultCode> results = new ArrayList<>();
    for (CollectionInfo collectionInfo : collectionInfoMap.values()) {
      results.add(collectionInfo.getReader().flush());
    }
    return CompletableResultCode.ofAll(results);
  }

  /**
   * Shutdown the provider. Calls {@link MetricReader#shutdown()} on all metric readers associated
   * with this provider. The resulting {@link CompletableResultCode} completes when all complete.
   */
  public CompletableResultCode shutdown() {
    if (!isClosed.compareAndSet(false, true)) {
      LOGGER.info("Multiple close calls");
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>();
    for (CollectionInfo info : collectionInfoMap.values()) {
      results.add(info.getReader().shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  /** Close the meter provider. Calls {@link #shutdown()} and blocks waiting for it to complete. */
  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
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
      // Suppress too-frequent-collection.
      long currentNanoTime = sharedState.getClock().nanoTime();
      long pastNanoTime = lastCollectionTimestamp.get();
      // It hasn't been long enough since the last collection.
      boolean disableSynchronousCollection =
          (currentNanoTime - pastNanoTime) < MINIMUM_COLLECTION_INTERVAL_NANOS;
      // If we're not disabling metrics, write the current collection time.
      // We don't care if this happens in more than one thread, suppression is optimistic, and the
      // interval is small enough some jitter isn't important.
      if (!disableSynchronousCollection) {
        lastCollectionTimestamp.lazySet(currentNanoTime);
      }

      List<MetricData> result = new ArrayList<>(meters.size());
      for (SdkMeter meter : meters) {
        result.addAll(
            meter.collectAll(
                collectionInfoMap.get(handle),
                sharedState.getClock().now(),
                disableSynchronousCollection));
      }
      return Collections.unmodifiableCollection(result);
    }
  }
}
