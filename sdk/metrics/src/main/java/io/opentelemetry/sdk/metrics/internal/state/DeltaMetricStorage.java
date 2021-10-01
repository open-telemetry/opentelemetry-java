/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Allows synchronous collection of metrics and reports delta values isolated by collection handle.
 *
 * <p>This storage should allow allocation of new aggregation cells for metrics and unique reporting
 * of delta accumulations per-collection-handle.
 */
@ThreadSafe
class DeltaMetricStorage<T> {
  private final Aggregator<T> aggregator;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T>> activeCollectionStorage =
      new ConcurrentHashMap<>();
  private final List<DeltaAccumulation<T>> unreportedDeltas = new ArrayList<>();
  private long lastCollectionTimeNanos = 0;

  // Minimum amount of time we allow between synchronous collections.
  // This meant to reduce overhead when multiple exporters attempt to read metrics quickly.
  // TODO: This should be configurable at the SDK level.
  private static final long MINIMUM_COLLECTION_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

  DeltaMetricStorage(Aggregator<T> aggregator) {
    this.aggregator = aggregator;
  }

  /**
   * Allocates memory for a new metric stream, and returns a handle for synchronous recordings.
   *
   * @param attributes The identifying attributes for the metric stream.
   * @return A handle that will (efficiently) record synchronous measurements.
   */
  public BoundStorageHandle bind(Attributes attributes) {
    AggregatorHandle<T> aggregatorHandle = activeCollectionStorage.get(attributes);
    if (aggregatorHandle != null && aggregatorHandle.acquire()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return aggregatorHandle;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    aggregatorHandle = aggregator.createHandle();
    while (true) {
      AggregatorHandle<?> boundAggregatorHandle =
          activeCollectionStorage.putIfAbsent(attributes, aggregatorHandle);
      if (boundAggregatorHandle != null) {
        if (boundAggregatorHandle.acquire()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return boundAggregatorHandle;
        }
        // Try to remove the boundAggregator. This will race with the collect method, but only one
        // will succeed.
        activeCollectionStorage.remove(attributes, boundAggregatorHandle);
        continue;
      }
      return aggregatorHandle;
    }
  }

  /**
   * Returns the latest delta accumulation for a specific collection handle.
   *
   * @param collector The current reader of metrics.
   * @param collectors All possible readers of metrics.
   * @param epochNanos the timestamp of the current collection in nanoseconds since Jan 1, 1970 GMT.
   * @return The delta accumulation of metrics since the last read of a the specified reader.
   */
  public synchronized Map<Attributes, T> collectFor(
      CollectionHandle collector, Set<CollectionHandle> collectors, long epochNanos) {
    // First we force a collection
    collectSynchronousDeltaAccumulationAndReset(epochNanos);
    // Now build a delta result.
    Map<Attributes, T> result = new HashMap<>();
    for (DeltaAccumulation<T> point : unreportedDeltas) {
      if (!point.wasReadBy(collector)) {
        mergeInPlace(result, point.read(collector), aggregator);
      }
    }
    // Now run a quick cleanup of deltas before returning.
    unreportedDeltas.removeIf(delta -> delta.wasReadByAll(collectors));
    return result;
  }

  /**
   * Collects the currently accumulated measurements from the concurrent-friendly synchronous
   * storage.
   *
   * <p>All synchronous handles will be collected + reset during this method. Additionally cleanup
   * related stale concurrent-map handles will occur. Any {@code null} measurements are ignored.
   */
  private synchronized void collectSynchronousDeltaAccumulationAndReset(long epochNanos) {
    // First check for a clock reset, and make sure we can limp along.
    if (epochNanos < lastCollectionTimeNanos) {
      // Reboot time tracking
      lastCollectionTimeNanos = 0;
    }
    // Next, make sure we don't poll/reset synchronous accumulations too quickly.
    if (epochNanos - lastCollectionTimeNanos < MINIMUM_COLLECTION_INTERVAL_NANOS) {
      // Skip collection, we already have recent data.
      return;
    }
    // Now we can go grab accumulated measurements.
    lastCollectionTimeNanos = epochNanos;
    Map<Attributes, T> result = new HashMap<>();
    for (Map.Entry<Attributes, AggregatorHandle<T>> entry : activeCollectionStorage.entrySet()) {
      boolean unmappedEntry = entry.getValue().tryUnmap();
      if (unmappedEntry) {
        // If able to unmap then remove the record from the current Map. This can race with the
        // acquire but because we requested a specific value only one will succeed.
        activeCollectionStorage.remove(entry.getKey(), entry.getValue());
      }
      T accumulation = entry.getValue().accumulateThenReset(entry.getKey());
      if (accumulation == null) {
        continue;
      }
      // Feed latest batch to the aggregator.
      result.put(entry.getKey(), accumulation);
    }
    if (!result.isEmpty()) {
      unreportedDeltas.add(new DeltaAccumulation<>(result));
    }
  }

  /**
   * Merges accumulations from {@code toMerge} into {@code result}.
   *
   * <p>Note: This mutates the result map.
   */
  static final <T> void mergeInPlace(
      Map<Attributes, T> result, Map<Attributes, T> toMerge, Aggregator<T> aggregator) {
    toMerge.forEach(
        (k, v) -> {
          if (result.containsKey(k)) {
            result.put(k, aggregator.merge(result.get(k), v));
          } else {
            result.put(k, v);
          }
        });
  }
}
