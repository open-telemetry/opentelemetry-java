/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.internal.state.MetricStorageUtils.MAX_ACCUMULATIONS;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.HashMap;
import java.util.Map;
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
class DeltaMetricStorage<T, U extends ExemplarData> {

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DeltaMetricStorage.class.getName()));
  private static final BoundStorageHandle NOOP_STORAGE_HANDLE = new NoopBoundHandle();

  private final Aggregator<T, U> aggregator;
  private final InstrumentDescriptor instrument;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T, U>> activeCollectionStorage =
      new ConcurrentHashMap<>();

  DeltaMetricStorage(Aggregator<T, U> aggregator, InstrumentDescriptor instrument) {
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
    AggregatorHandle<T, U> aggregatorHandle = activeCollectionStorage.get(attributes);
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
      AggregatorHandle<T, U> boundAggregatorHandle =
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
   * Collects the currently accumulated measurements from the concurrent-friendly synchronous
   * storage.
   *
   * <p>All synchronous handles will be collected + reset during this method. Any {@code null}
   * measurements are ignored.
   */
  public synchronized Map<Attributes, T> collect() {
    // Grab accumulated measurements.
    Map<Attributes, T> accumulations = new HashMap<>();
    for (Map.Entry<Attributes, AggregatorHandle<T, U>> entry : activeCollectionStorage.entrySet()) {
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
      accumulations.put(entry.getKey(), accumulation);
    }
    return accumulations;
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
