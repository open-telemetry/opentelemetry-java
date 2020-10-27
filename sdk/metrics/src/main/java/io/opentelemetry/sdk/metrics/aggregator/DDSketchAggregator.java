/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.datadoghq.sketch.ddsketch.DDSketch;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

@ThreadSafe
public final class DDSketchAggregator extends AbstractAggregator {

  private static final AggregatorFactory AGGREGATOR_FACTORY =
      () -> new DDSketchAggregator(() -> DDSketch.balanced(0.01));

  public static AggregatorFactory getBalancedFactory() {
    return AGGREGATOR_FACTORY;
  }

  // Since DDSketch is not thread safe, this queue is used to buffer calls to record, reducing
  // the need to take a lock for every call. (With the downside of having to box the double.)
  private final ArrayBlockingQueue<Double> pendingValues = new ArrayBlockingQueue<>(64);

  private final Supplier<DDSketch> sketchSupplier;

  @GuardedBy("lock")
  private volatile DDSketch current;

  @GuardedBy("lock")
  private volatile double sum = 0;

  private final Object lock = new Object();

  private DDSketchAggregator(@Nonnull Supplier<DDSketch> sketchSupplier) {
    this.sketchSupplier = sketchSupplier;
    current = sketchSupplier.get();
    assert current != null;
  }

  @Override
  void doMergeAndReset(Aggregator target) {
    DDSketch oldSketch;
    double oldSum;
    synchronized (lock) {
      drain();
      oldSketch = current;
      oldSum = sum;
      current = sketchSupplier.get();
      sum = 0;
    }

    ((DDSketchAggregator) target).mergeFrom(oldSketch, oldSum);
  }

  private void mergeFrom(DDSketch other, double oldSum) {
    synchronized (lock) {
      current.mergeWith(other);
      sum += oldSum;
    }
  }

  @Nullable
  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    synchronized (lock) {
      drain();
      long count = (long) current.getCount();
      return count == 0
          ? null
          : SummaryPoint.create(
              startEpochNanos,
              epochNanos,
              labels,
              count,
              sum,
              Arrays.asList(
                  ValueAtPercentile.create(0.0, current.getMinValue()),
                  ValueAtPercentile.create(100.0, current.getMaxValue())));
    }
  }

  @Override
  protected void doRecordDouble(double value) {
    // autoboxing seems better than blocking
    if (!pendingValues.offer(value)) {
      // queue is full, so we must add the value directly to the sketch.
      synchronized (lock) {
        // We have a lock, might as well drain the queue.
        drain();
        add(value);
      }
    }
  }

  /** Must be called under lock. */
  private void drain() {
    assert Thread.holdsLock(lock);
    for (Double value = pendingValues.poll(); value != null; value = pendingValues.poll()) {
      add(value);
    }
  }

  /** Must be called under lock. */
  private void add(double value) {
    assert Thread.holdsLock(lock);
    current.accept(value);
    sum += value;
  }
}
