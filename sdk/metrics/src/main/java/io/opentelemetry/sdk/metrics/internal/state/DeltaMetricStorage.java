/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.internal.state.MetricStorageUtils.MAX_ACCUMULATIONS;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Allows synchronous collection of metrics and reports delta values isolated by collection handle.
 *
 * <p>This storage should allow allocation of new aggregation cells for metrics and unique reporting
 * of delta accumulations per-collection-handle.
 */
@ThreadSafe
class DeltaMetricStorage<T> {

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DeltaMetricStorage.class.getName()));
  private static final BoundStorageHandle NOOP_STORAGE_HANDLE = new NoopBoundHandle();

  private final Aggregator<T> aggregator;
  private final InstrumentDescriptor instrument;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T>> activeCollectionStorage =
      new ConcurrentHashMap<>();
  private final List<DeltaAccumulation<T>> unreportedDeltas = new ArrayList<>();

  DeltaMetricStorage(Aggregator<T> aggregator, InstrumentDescriptor instrument) {
    this.aggregator = aggregator;
    this.instrument = instrument;
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

    // Missing entry or no longer mapped. Try to add a new one if not exceeded cardinality limits.
    aggregatorHandle = aggregator.createHandle();
    while (true) {
      if (activeCollectionStorage.size() >= MAX_ACCUMULATIONS) {
        logger.log(
            Level.WARNING,
            "Instrument "
                + instrument.getName()
                + " has exceeded the maximum allowed accumulations ("
                + MAX_ACCUMULATIONS
                + ").");
        return NOOP_STORAGE_HANDLE;
      }
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
   * @param suppressCollection If true, don't actively pull synchronous instruments, measurements
   *     should be up to date.
   * @return The delta accumulation of metrics since the last read of the specified reader.
   */
  public synchronized Map<Attributes, T> collectFor(
      CollectionHandle collector, Set<CollectionHandle> collectors, boolean suppressCollection) {
    // First we force a collection
    if (!suppressCollection) {
      collectSynchronousDeltaAccumulationAndReset();
    }
    // Now build a delta result.
    Map<Attributes, T> result = new HashMap<>();
    for (DeltaAccumulation<T> point : unreportedDeltas) {
      if (!point.wasReadBy(collector)) {
        MetricStorageUtils.mergeInPlace(result, point.read(collector), aggregator);
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
  private synchronized void collectSynchronousDeltaAccumulationAndReset() {
    // Grab accumulated measurements.
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

  /** An implementation of {@link BoundStorageHandle} that does not record. */
  private static class NoopBoundHandle implements BoundStorageHandle {

    @Override
    public void recordLong(long value, Attributes attributes, Context context) {}

    @Override
    public void recordDouble(double value, Attributes attributes, Context context) {}

    @Override
    public void release() {}
  }
}
