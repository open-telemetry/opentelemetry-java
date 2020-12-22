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
import java.util.function.Function;

final class SynchronousInstrumentAccumulator<B extends AbstractBoundInstrument> {
  private final ConcurrentHashMap<Labels, B> boundLabels;
  private final ReentrantLock collectLock;
  private final InstrumentProcessor instrumentProcessor;
  private final Function<Aggregator, B> boundFactory;

  SynchronousInstrumentAccumulator(
      InstrumentProcessor instrumentProcessor, Function<Aggregator, B> boundFactory) {
    this.boundFactory = boundFactory;
    boundLabels = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.instrumentProcessor = instrumentProcessor;
  }

  public B bind(Labels labels) {
    Objects.requireNonNull(labels, "labels");
    B binding = boundLabels.get(labels);
    if (binding != null && binding.bind()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return binding;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    binding = boundFactory.apply(instrumentProcessor.getAggregator());
    while (true) {
      B oldBound = boundLabels.putIfAbsent(labels, binding);
      if (oldBound != null) {
        if (oldBound.bind()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return oldBound;
        }
        // Try to remove the oldBound. This will race with the collect method, but only one will
        // succeed.
        boundLabels.remove(labels, oldBound);
        continue;
      }
      return binding;
    }
  }

  /**
   * Collects records from all the entries (labelSet, Bound) that changed since the last collect()
   * call.
   */
  public final List<MetricData> collectAll() {
    collectLock.lock();
    try {
      for (Map.Entry<Labels, B> entry : boundLabels.entrySet()) {
        boolean unmappedEntry = entry.getValue().tryUnmap();
        if (unmappedEntry) {
          // If able to unmap then remove the record from the current Map. This can race with the
          // acquire but because we requested a specific value only one will succeed.
          boundLabels.remove(entry.getKey(), entry.getValue());
        }
        Accumulation accumulation = entry.getValue().getAggregator().accumulateThenReset();
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
