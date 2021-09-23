/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SynchronousMetricStorage<T> implements MetricStorage, WriteableMetricStorage {
  private final MetricDescriptor metricDescriptor;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorLabels;
  private final ReentrantLock collectLock;
  private final Aggregator<T> aggregator;
  private final AttributesProcessor attributesProcessor;

  @GuardedBy("collectLock")
  private final List<DeltaAccumulation<T>> savedDeltas;

  @GuardedBy("collectLock")
  private final Map<CollectionHandle, LastReportedAccumulation<T>> reportHistory;

  /** Constructs metric storage for a given synchronous instrument and view. */
  public static <T> SynchronousMetricStorage<T> create(
      View view,
      InstrumentDescriptor instrumentDescriptor,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      ExemplarSampler sampler) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrumentDescriptor);
    final Aggregator<T> aggregator =
        view.getAggregation()
            .config(instrumentDescriptor)
            .create(
                resource,
                instrumentationLibraryInfo,
                instrumentDescriptor,
                metricDescriptor,
                () -> sampler.createReservoir(view.getAggregation()));
    return new SynchronousMetricStorage<>(
        metricDescriptor, aggregator, view.getAttributesProcessor());
  }

  SynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor) {
    this.metricDescriptor = metricDescriptor;
    aggregatorLabels = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.reportHistory = new HashMap<>();
    this.savedDeltas = new ArrayList<>();
  }

  // This is a storage handle to use when the attributes processor requires
  private final BoundStorageHandle lateBoundStorageHandle =
      new BoundStorageHandle() {
        @Override
        public void release() {}

        @Override
        public void recordLong(long value, Attributes attributes, Context context) {
          SynchronousMetricStorage.this.recordLong(value, attributes, context);
        }

        @Override
        public void recordDouble(double value, Attributes attributes, Context context) {
          SynchronousMetricStorage.this.recordDouble(value, attributes, context);
        }
      };

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    if (attributesProcessor.usesContext()) {
      // We cannot pre-bind attributes because we need to pull attributes from context.
      return lateBoundStorageHandle;
    }
    return doBind(attributesProcessor.process(attributes, Context.current()));
  }

  private BoundStorageHandle doBind(Attributes attributes) {
    AggregatorHandle<T> aggregatorHandle = aggregatorLabels.get(attributes);
    if (aggregatorHandle != null && aggregatorHandle.acquire()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return aggregatorHandle;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    aggregatorHandle = aggregator.createHandle();
    while (true) {
      AggregatorHandle<?> boundAggregatorHandle =
          aggregatorLabels.putIfAbsent(attributes, aggregatorHandle);
      if (boundAggregatorHandle != null) {
        if (boundAggregatorHandle.acquire()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return boundAggregatorHandle;
        }
        // Try to remove the boundAggregator. This will race with the collect method, but only one
        // will succeed.
        aggregatorLabels.remove(attributes, boundAggregatorHandle);
        continue;
      }
      return aggregatorHandle;
    }
  }

  // Overridden to make sure attributes processor can pull baggage.
  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    BoundStorageHandle handle = doBind(attributes);
    try {
      handle.recordLong(value, attributes, context);
    } finally {
      handle.release();
    }
  }

  // Overridden to make sure attributes processor can pull baggage.
  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    BoundStorageHandle handle = doBind(attributes);
    try {
      handle.recordDouble(value, attributes, context);
    } finally {
      handle.release();
    }
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  /**
   * This method leverages the `savedDeltas` and `reportHistory` to construct a final metric point
   * to report for this synchronous instrument.
   */
  @GuardedBy("collectLock")
  @Nullable
  private MetricData buildMetricFor(
      CollectionHandle collector, long startEpochNanos, long epochNanos) {
    Map<Attributes, T> result = new HashMap<>();
    // Next merge the delta w/ the last set of points.
    for (DeltaAccumulation<T> point : savedDeltas) {
      if (!point.wasReadBy(collector)) {
        mergeInPlace(result, point.read(collector));
      }
    }

    long lastEpochNanos;
    // First pull the last cumulative value.
    if (reportHistory.containsKey(collector)) {
      LastReportedAccumulation<T> last = reportHistory.get(collector);
      // Send the accumulated deltas in w/ previous accumulation to get final result.
      if (aggregator.isStateful()) {
        mergeInPlace(result, last.getAccumlation());
      }
      lastEpochNanos = last.getEpochNanos();
    } else {
      lastEpochNanos = startEpochNanos;
    }

    // Now write the aggregated value back, and generate final metric.
    reportHistory.put(collector, new LastReportedAccumulation<>(result, epochNanos));
    // Don't make a metric if we have no values.
    if (result.isEmpty()) {
      return null;
    }
    return aggregator.toMetricData(result, startEpochNanos, lastEpochNanos, epochNanos);
  }

  /** Merges accumulations from {@code toMerge} into {@code result}. */
  private void mergeInPlace(Map<Attributes, T> result, Map<Attributes, T> toMerge) {
    toMerge.forEach(
        (k, v) -> {
          if (result.containsKey(k)) {
            result.put(k, aggregator.merge(result.get(k), v));
          } else {
            result.put(k, v);
          }
        });
  }

  /** Removes deltas once all collectors have pulled them. */
  @GuardedBy("collectLock")
  private void cleanup(Set<CollectionHandle> collectors) {
    Iterator<DeltaAccumulation<T>> i = savedDeltas.iterator();
    while (i.hasNext()) {
      DeltaAccumulation<T> delta = i.next();
      if (delta.wasReadyByAll(collectors)) {
        i.remove();
      }
    }
    // TODO: Do we allow different pipelines?
    Iterator<CollectionHandle> c = reportHistory.keySet().iterator();
    while (c.hasNext()) {
      if (!collectors.contains(c.next())) {
        c.remove();
      }
    }
  }

  /** Collects bucketed metrics and resets the underlying storage for the next collection period. */
  @Override
  @Nullable
  public MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      long startEpochNanos,
      long epochNanos) {
    collectLock.lock();
    try {
      // First reset currently accumulating synchronous handles.
      savedDeltas.add(collectSynchronousDeltaAccumulationAndReset());
      // Next build metric from past history and latest deltas.
      MetricData result = buildMetricFor(collector, startEpochNanos, epochNanos);
      // finally, cleanup stale deltas.
      cleanup(allCollectors);
      return result;
    } finally {
      collectLock.unlock();
    }
  }

  /**
   * Collects the currently accumulated measurements from the concurrent-friendly synchronous
   * storage.
   *
   * <p>All synchronous handles will be collected + reset during this method. Additionally cleanup
   * related stale concurrent-map handles will occur. Any {@code null} measurements are ignored.
   *
   * <p>This method should be behind a lock.
   */
  @GuardedBy("collectLock")
  private DeltaAccumulation<T> collectSynchronousDeltaAccumulationAndReset() {
    Map<Attributes, T> result = new HashMap<>();
    for (Map.Entry<Attributes, AggregatorHandle<T>> entry : aggregatorLabels.entrySet()) {
      boolean unmappedEntry = entry.getValue().tryUnmap();
      if (unmappedEntry) {
        // If able to unmap then remove the record from the current Map. This can race with the
        // acquire but because we requested a specific value only one will succeed.
        aggregatorLabels.remove(entry.getKey(), entry.getValue());
      }
      T accumulation = entry.getValue().accumulateThenReset(entry.getKey());
      if (accumulation == null) {
        continue;
      }
      // Feed latest batch to the aggregator.
      result.put(entry.getKey(), accumulation);
    }
    return new DeltaAccumulation<>(result);
  }

  /** Remembers what was presented to a specific exporter. */
  private static class LastReportedAccumulation<T> {
    private final Map<Attributes, T> accumulation;
    private final long epochNanos;

    LastReportedAccumulation(Map<Attributes, T> accumulation, long epochNanos) {
      this.accumulation = accumulation;
      this.epochNanos = epochNanos;
    }

    long getEpochNanos() {
      return epochNanos;
    }

    Map<Attributes, T> getAccumlation() {
      return accumulation;
    }
  }
}
