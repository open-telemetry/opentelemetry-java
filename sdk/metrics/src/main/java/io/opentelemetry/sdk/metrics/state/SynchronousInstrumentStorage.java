/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.SynchronousHandle;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.Measurement;
import io.opentelemetry.sdk.metrics.view.AttributesProcessor;
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
  private final Aggregator<T> aggregator;
  private final AttributesProcessor attributesProcessor;

  public static <T> SynchronousInstrumentStorage<T> create(
      Aggregator<T> aggregator, AttributesProcessor attributesProcessor) {
    return new SynchronousInstrumentStorage<T>(aggregator, attributesProcessor);
  }

  SynchronousInstrumentStorage(Aggregator<T> aggregator, AttributesProcessor attributesProcessor) {
    perAttrributeStorage = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
  }

  private final StorageHandle lateBoundStorageHandle =
      new StorageHandle() {
        @Override
        public void record(Measurement measurement) {
          SynchronousInstrumentStorage.this.record(measurement);
        }

        @Override
        public void release() {}
      };

  /**
   * Obtain exclusive write access to metric stream for this instrument defined by this set of
   * attributes.
   */
  @Override
  public StorageHandle bind(Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    if (attributesProcessor.usesContext()) {
      return lateBoundStorageHandle;
    }
    return doBind(attributesProcessor.process(attributes, Context.current()));
  }

  /** version of "bind" that does NOT call attributesProcessor. */
  private StorageHandle doBind(Attributes attributes) {
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
    StorageHandle handle =
        doBind(attributesProcessor.process(measurement.getAttributes(), measurement.getContext()));
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
