/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

final class SynchronousInstrumentAccumulator {
  private final ConcurrentHashMap<Labels, Aggregator> aggregatorLabels;
  private final ReentrantLock collectLock;
  private final InstrumentProcessor instrumentProcessor;

  SynchronousInstrumentAccumulator(InstrumentProcessor instrumentProcessor) {
    aggregatorLabels = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.instrumentProcessor = instrumentProcessor;
  }

  Aggregator bind(Labels labels) {
    Objects.requireNonNull(labels, "labels");
    Aggregator aggregator = aggregatorLabels.get(labels);
    if (aggregator != null && aggregator.acquire()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return aggregator;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    aggregator = instrumentProcessor.getAggregator();
    while (true) {
      Aggregator boundAggregator = aggregatorLabels.putIfAbsent(labels, aggregator);
      if (boundAggregator != null) {
        if (boundAggregator.acquire()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return boundAggregator;
        }
        // Try to remove the boundAggregator. This will race with the collect method, but only one
        // will succeed.
        aggregatorLabels.remove(labels, boundAggregator);
        continue;
      }
      return aggregator;
    }
  }

  /**
   * Collects records from all the entries (labelSet, Bound) that changed since the last collect()
   * call.
   */
  public final List<MetricData> collectAll() {
    collectLock.lock();
    try {
      for (Map.Entry<Labels, Aggregator> entry : aggregatorLabels.entrySet()) {
        boolean unmappedEntry = entry.getValue().tryUnmap();
        if (unmappedEntry) {
          // If able to unmap then remove the record from the current Map. This can race with the
          // acquire but because we requested a specific value only one will succeed.
          aggregatorLabels.remove(entry.getKey(), entry.getValue());
        }
        Accumulation accumulation = entry.getValue().accumulateThenReset();
        if (accumulation == null) {
          continue;
        }
        instrumentProcessor.batch(entry.getKey(), accumulation);
      }
      return instrumentProcessor.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }
}
