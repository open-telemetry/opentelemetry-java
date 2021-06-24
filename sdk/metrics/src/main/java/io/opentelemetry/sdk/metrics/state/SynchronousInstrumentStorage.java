/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.SynchronousHandle;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.instrument.Measurement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Storage for measurements from a synchronous instrument.
 *
 * <p>This class leverages a {@link MetricStreamProcessor} for the specific instrument + aggregation
 * desired.
 */
final class SynchronousInstrumentStorage<T> implements WriteableInstrumentStorage {
  private final ConcurrentHashMap<Attributes, SynchronousHandle<T>> perAttrributeStorage;
  private final ReentrantLock collectLock;
  private final InstrumentDescriptor descriptor;
  private final Aggregator<T> aggregator;

  public static <T> SynchronousInstrumentStorage<T> create(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregator<T> aggregator) {
    return new SynchronousInstrumentStorage<T>(descriptor, aggregator);
  }

  SynchronousInstrumentStorage(InstrumentDescriptor descriptor, Aggregator<T> aggregator) {
    perAttrributeStorage = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.descriptor = descriptor;
    this.aggregator = aggregator;
  }

  @Override
  public InstrumentDescriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Obtain exclusive write access to metric stream for this instrument defined by this set of
   * attributes.
   */
  @Override
  public SynchronousHandle<T> bind(Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    // TODO - equivalent of labels processor?
    SynchronousHandle<T> storageHandle = perAttrributeStorage.get(attributes);
    if (storageHandle != null && storageHandle.acquire()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return storageHandle;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    storageHandle = aggregator.createStreamStorage();
    while (true) {
      SynchronousHandle<T> boundStorageHandle =
          perAttrributeStorage.putIfAbsent(attributes, storageHandle);
      if (boundStorageHandle != null) {
        if (boundStorageHandle.acquire()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return boundStorageHandle;
        }
        // Try to remove the boundAggregator. This will race with the collect method, but only one
        // will succeed.
        perAttrributeStorage.remove(attributes, boundStorageHandle);
        continue;
      }
      return storageHandle;
    }
  }

  /** Writes a measurement into the appropriate metric stream. */
  @Override
  public void record(Measurement measurement) {
    SynchronousHandle<T> handle = bind(measurement.getAttributes());
    try {
      handle.record(measurement);
    } finally {
      handle.release();
    }
  }

  /** Collects bucketed metrics and resets the underlying storage for the next collection period. */
  @Override
  public List<MetricData> collectAndReset(long epochNanos) {
    collectLock.lock();
    try {
      for (Map.Entry<Attributes, SynchronousHandle<T>> entry : perAttrributeStorage.entrySet()) {
        boolean unmappedEntry = entry.getValue().tryUnmap();
        if (unmappedEntry) {
          // If able to unmap then remove the record from the current Map. This can race with the
          // acquire but because we requested a specific value only one will succeed.
          perAttrributeStorage.remove(entry.getKey(), entry.getValue());
        }
        T accumulation = entry.getValue().accumulateThenReset();
        if (accumulation == null) {
          continue;
        }
        // Feed latest batch to the aggregator.
        aggregator.batchStreamAccumulation(entry.getKey(), accumulation);
      }
      return aggregator.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }
}
