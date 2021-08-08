/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Stores aggregated {@link MetricData} for synchronous instruments. */
public final class SynchronousMetricStorage<T> implements WriteableMetricStorage {
  private final MetricDescriptor metricDescriptor;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorLabels;
  private final ReentrantLock collectLock;
  private final Aggregator<T> aggregator;
  private final InstrumentProcessor<T> instrumentProcessor;
  private final LabelsProcessor labelsProcessor;

  /** Constructs metric storage for a given synchronous instrument. */
  public static <T> SynchronousMetricStorage<T> create(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {
    Aggregator<T> aggregator = meterProviderSharedState.getAggregator(meterSharedState, descriptor);
    return new SynchronousMetricStorage<>(
        // TODO: View can change metric name/description.  Update this when wired in.
        MetricDescriptor.create(
            descriptor.getName(), descriptor.getDescription(), descriptor.getUnit()),
        aggregator,
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos()),
        meterProviderSharedState.getLabelsProcessor(meterSharedState, descriptor));
  }

  SynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      InstrumentProcessor<T> instrumentProcessor,
      LabelsProcessor labelsProcessor) {
    this.metricDescriptor = metricDescriptor;
    aggregatorLabels = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.aggregator = aggregator;
    this.instrumentProcessor = instrumentProcessor;
    this.labelsProcessor = labelsProcessor;
  }

  @Override
  public BoundStorageHandle bind(Attributes labels) {
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
  public MetricData collectAndReset(long startEpochNanos, long epochNanos) {
    collectLock.lock();
    try {
      for (Map.Entry<Attributes, AggregatorHandle<T>> entry : aggregatorLabels.entrySet()) {
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

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
