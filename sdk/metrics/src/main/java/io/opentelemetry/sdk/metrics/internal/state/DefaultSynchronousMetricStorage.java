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
    AggregatorHolder<T, U> aggregatorHolder = getHolderForRecord();
    try {
      AggregatorHandle<T, U> handle =
          getAggregatorHandle(aggregatorHolder.aggregatorHandles, attributes, context);
      handle.recordLong(value, attributes, context);
    } finally {
      releaseHolderForRecord(aggregatorHolder);
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
    AggregatorHolder<T, U> aggregatorHolder = getHolderForRecord();
    try {
      AggregatorHandle<T, U> handle =
          getAggregatorHandle(aggregatorHolder.aggregatorHandles, attributes, context);
      handle.recordDouble(value, attributes, context);
    } finally {
      releaseHolderForRecord(aggregatorHolder);
    }
  }

  /**
   * Obtain the AggregatorHolder for recording measurements, re-reading the volatile
   * this.aggregatorHolder until we access one where recordsInProgress is even. Collect sets
   * recordsInProgress to odd as a signal that AggregatorHolder is stale and is being replaced.
   * Record operations increment recordInProgress by 2. Callers MUST call {@link
   * #releaseHolderForRecord(AggregatorHolder)} when record operation completes to signal to that
   * its safe to proceed with Collect operations.
   */
  private AggregatorHolder<T, U> getHolderForRecord() {
    do {
      AggregatorHolder<T, U> aggregatorHolder = this.aggregatorHolder;
      int recordsInProgress = aggregatorHolder.activeRecordingThreads.addAndGet(2);
      if (recordsInProgress % 2 == 0) {
        return aggregatorHolder;
      } else {
        // Collect is in progress, decrement recordsInProgress to allow collect to proceed and
        // re-read aggregatorHolder
        aggregatorHolder.activeRecordingThreads.addAndGet(-2);
      }
    } while (true);
  }

  /**
   * Called on the {@link AggregatorHolder} obtained from {@link #getHolderForRecord()} to indicate
   * that recording is complete and it is safe to collect.
   */
  private void releaseHolderForRecord(AggregatorHolder<T, U> aggregatorHolder) {
    aggregatorHolder.activeRecordingThreads.addAndGet(-2);
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
      // Increment recordsInProgress by 1, which produces an odd number acting as a signal that
      // record operations should re-read the volatile this.aggregatorHolder.
      // Repeatedly grab recordsInProgress until it is <= 1, which signals all active record
      // operations are complete.
      int recordsInProgress = holder.activeRecordingThreads.addAndGet(1);
      while (recordsInProgress > 1) {
        recordsInProgress = holder.activeRecordingThreads.get();
      }
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
    // Recording threads grab the current interval (AggregatorHolder) and atomically increment
    // this by 2 before recording against it (and then decrement by two when done).
    //
    // The collection thread grabs the current interval (AggregatorHolder) and atomically
    // increments this by 1 to "lock" this interval (and then waits for any active recording
    // threads to complete before collecting it).
    //
    // Recording threads check the return value of their atomic increment, and if it's odd
    // that means the collector thread has "locked" this interval for collection.
    //
    // But before the collector "locks" the interval it sets up a new current interval
    // (AggregatorHolder), and so if a recording thread encounters an odd value,
    // all it needs to do is release the "read lock" it just obtained (decrementing by 2),
    // and then grab and record against the new current interval (AggregatorHolder).
    private final AtomicInteger activeRecordingThreads = new AtomicInteger(0);
  }
}
