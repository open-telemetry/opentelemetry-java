/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

final class SynchronousInstrumentAccumulator<T> extends AbstractAccumulator {
  private final ConcurrentHashMap<Labels, AggregatorHandle<T>> aggregatorLabels;
  private final ReentrantLock collectLock;
  private final Aggregator<T> aggregator;
  private final InstrumentProcessor<T> instrumentProcessor;
  private final LabelsProcessor labelsProcessor;

  static <T> SynchronousInstrumentAccumulator<T> create(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {
    Aggregator<T> aggregator =
        getAggregator(meterProviderSharedState, meterSharedState, descriptor);
    return new SynchronousInstrumentAccumulator<>(
        aggregator,
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos()),
        getLabelsProcessor(meterProviderSharedState, descriptor));
  }

  SynchronousInstrumentAccumulator(
      Aggregator<T> aggregator,
      InstrumentProcessor<T> instrumentProcessor,
      LabelsProcessor labelsProcessor) {
    aggregatorLabels = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.aggregator = aggregator;
    this.instrumentProcessor = instrumentProcessor;
    this.labelsProcessor = labelsProcessor;
  }

  AggregatorHandle<?> bind(Labels labels) {
    Objects.requireNonNull(labels, "labels");
    labels = labelsProcessor.onLabelsBound(Context.current(), labels);
    AggregatorHandle<T> aggregatorHandle = aggregatorLabels.get(labels);
    if (aggregatorHandle != null && aggregatorHandle.acquire()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return aggregatorHandle;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    aggregatorHandle = aggregator.createHandle();
    while (true) {
      AggregatorHandle<?> boundAggregatorHandle =
          aggregatorLabels.putIfAbsent(labels, aggregatorHandle);
      if (boundAggregatorHandle != null) {
        if (boundAggregatorHandle.acquire()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return boundAggregatorHandle;
        }
        // Try to remove the boundAggregator. This will race with the collect method, but only one
        // will succeed.
        aggregatorLabels.remove(labels, boundAggregatorHandle);
        continue;
      }
      return aggregatorHandle;
    }
  }

  @Override
  List<MetricData> collectAll(long epochNanos) {
    collectLock.lock();
    try {
      for (Map.Entry<Labels, AggregatorHandle<T>> entry : aggregatorLabels.entrySet()) {
        boolean unmappedEntry = entry.getValue().tryUnmap();
        if (unmappedEntry) {
          // If able to unmap then remove the record from the current Map. This can race with the
          // acquire but because we requested a specific value only one will succeed.
          aggregatorLabels.remove(entry.getKey(), entry.getValue());
        }
        T accumulation = entry.getValue().accumulateThenReset();
        if (accumulation == null) {
          continue;
        }
        instrumentProcessor.batch(entry.getKey(), accumulation);
      }
      return instrumentProcessor.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }
}
