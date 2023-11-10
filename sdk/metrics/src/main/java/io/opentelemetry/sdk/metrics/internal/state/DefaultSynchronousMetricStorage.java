/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultSynchronousMetricStorage<T extends PointData, U extends ExemplarData>
    implements SynchronousMetricStorage {

  private static final Logger internalLogger =
      Logger.getLogger(DefaultSynchronousMetricStorage.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);
  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  private final Aggregator<T, U> aggregator;
  private volatile AggregatorHolder<T, U> aggregatorHolder = new AggregatorHolder<>();
  private final AttributesProcessor attributesProcessor;

  /**
   * This field is set to 1 less than the actual intended cardinality limit, allowing the last slot
   * to be filled by the {@link MetricStorage#CARDINALITY_OVERFLOW} series.
   */
  private final int maxCardinality;

  private final ConcurrentLinkedQueue<AggregatorHandle<T, U>> aggregatorHandlePool =
      new ConcurrentLinkedQueue<>();

  DefaultSynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      Aggregator<T, U> aggregator,
      AttributesProcessor attributesProcessor,
      int maxCardinality) {
    this.registeredReader = registeredReader;
    this.metricDescriptor = metricDescriptor;
    this.aggregationTemporality =
        registeredReader
            .getReader()
            .getAggregationTemporality(metricDescriptor.getSourceInstrument().getType());
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.maxCardinality = maxCardinality - 1;
  }

  // Visible for testing
  Queue<AggregatorHandle<T, U>> getAggregatorHandlePool() {
    return aggregatorHandlePool;
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    // Obtain the AggregatorHolder, re-reading the volatile this.aggregatorHolder until we access
    // one where recordsInProgress != -1. Collect sets recordsInProgress to -1 as a signal that
    // AggregatorHolder is stale and is being replaced. Record operations increment recordInProgress
    // and decrement when complete as a signal to Collect that record operations are active and must
    // complete before its safe to collect.
    AggregatorHolder<T, U> aggregatorHolder;
    do {
      aggregatorHolder = this.aggregatorHolder;
    } while (aggregatorHolder.recordsInProgress.updateAndGet(
            operand -> operand < 0 ? operand : operand + 1)
        < 0);
    try {
      AggregatorHandle<T, U> handle =
          getAggregatorHandle(aggregatorHolder.aggregatorHandles, attributes, context);
      handle.recordLong(value, attributes, context);
    } finally {
      aggregatorHolder.recordsInProgress.decrementAndGet();
    }
  }

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    if (Double.isNaN(value)) {
      logger.log(
          Level.FINE,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has recorded measurement Not-a-Number (NaN) value with attributes "
              + attributes
              + ". Dropping measurement.");
      return;
    }
    // Obtain the AggregatorHolder, re-reading the volatile this.aggregatorHolder until we access
    // one where recordsInProgress != -1. Collect sets recordsInProgress to -1 as a signal that
    // AggregatorHolder is stale and is being replaced. Record operations increment recordInProgress
    // and decrement when complete as a signal to Collect that record operations are active and must
    // complete before its safe to collect.
    AggregatorHolder<T, U> aggregatorHolder;
    do {
      aggregatorHolder = this.aggregatorHolder;
    } while (aggregatorHolder.recordsInProgress.updateAndGet(
            operand -> operand < 0 ? operand : operand + 1)
        < 0);
    try {
      AggregatorHandle<T, U> handle =
          getAggregatorHandle(aggregatorHolder.aggregatorHandles, attributes, context);
      handle.recordDouble(value, attributes, context);
    } finally {
      aggregatorHolder.recordsInProgress.decrementAndGet();
    }
  }

  private AggregatorHandle<T, U> getAggregatorHandle(
      ConcurrentHashMap<Attributes, AggregatorHandle<T, U>> aggregatorHandles,
      Attributes attributes,
      Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    AggregatorHandle<T, U> handle = aggregatorHandles.get(attributes);
    if (handle != null) {
      return handle;
    }
    if (aggregatorHandles.size() >= maxCardinality) {
      logger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      // Return handle for overflow series, first checking if a handle already exists for it
      attributes = MetricStorage.CARDINALITY_OVERFLOW;
      handle = aggregatorHandles.get(attributes);
      if (handle != null) {
        return handle;
      }
    }
    // Get handle from pool if available, else create a new one.
    AggregatorHandle<T, U> newHandle = aggregatorHandlePool.poll();
    if (newHandle == null) {
      newHandle = aggregator.createHandle();
    }
    handle = aggregatorHandles.putIfAbsent(attributes, newHandle);
    return handle != null ? handle : newHandle;
  }

  @Override
  public MetricData collect(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos) {
    boolean reset = aggregationTemporality == AggregationTemporality.DELTA;
    long start =
        aggregationTemporality == AggregationTemporality.DELTA
            ? registeredReader.getLastCollectEpochNanos()
            : startEpochNanos;

    ConcurrentHashMap<Attributes, AggregatorHandle<T, U>> aggregatorHandles;
    if (reset) {
      AggregatorHolder<T, U> holder = this.aggregatorHolder;
      this.aggregatorHolder = new AggregatorHolder<>();
      // Set recordsInProgress to -1, which causes record operations to re-read the volatile
      // this.aggregatorHolder
      // Use compareAndSet in a loop to confirm we only set recordsInProgress if there aren't any
      // records operations in progress.
      while (!holder.recordsInProgress.compareAndSet(0, -1)) {}
      aggregatorHandles = holder.aggregatorHandles;
    } else {
      aggregatorHandles = this.aggregatorHolder.aggregatorHandles;
    }

    // Grab aggregated points.
    List<T> points = new ArrayList<>(aggregatorHandles.size());
    aggregatorHandles.forEach(
        (attributes, handle) -> {
          T point = handle.aggregateThenMaybeReset(start, epochNanos, attributes, reset);
          if (reset) {
            // Return the aggregator to the pool.
            aggregatorHandlePool.offer(handle);
          }
          if (point != null) {
            points.add(point);
          }
        });

    // Trim pool down if needed. pool.size() will only exceed maxCardinality if new handles are
    // created during collection.
    int toDelete = aggregatorHandlePool.size() - (maxCardinality + 1);
    for (int i = 0; i < toDelete; i++) {
      aggregatorHandlePool.poll();
    }

    if (points.isEmpty()) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, aggregationTemporality);
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  private static class AggregatorHolder<T extends PointData, U extends ExemplarData> {
    private final ConcurrentHashMap<Attributes, AggregatorHandle<T, U>> aggregatorHandles =
        new ConcurrentHashMap<>();
    private final AtomicInteger recordsInProgress = new AtomicInteger(0);
  }
}
