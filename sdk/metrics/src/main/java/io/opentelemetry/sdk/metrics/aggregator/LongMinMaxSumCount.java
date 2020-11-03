/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class LongMinMaxSumCount extends AbstractAggregator {

  private static final AggregatorFactory AGGREGATOR_FACTORY = LongMinMaxSumCount::new;

  // The current value. This controls its own internal thread-safety via method access. Don't
  // try to use its fields directly.
  private final LongSummary current = new LongSummary();

  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  private LongMinMaxSumCount() {}

  @Override
  void doMergeAndReset(Aggregator target) {
    LongMinMaxSumCount other = (LongMinMaxSumCount) target;

    current.mergeAndReset(other.current);
  }

  @Nullable
  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return current.toPoint(startEpochNanos, epochNanos, labels);
  }

  @Override
  public void doRecordLong(long value) {
    current.record(value);
  }

  private static final class LongSummary {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("lock")
    private long sum = 0;

    @GuardedBy("lock")
    private long count = 0;

    @GuardedBy("lock")
    private long min = Long.MAX_VALUE;

    @GuardedBy("lock")
    private long max = Long.MIN_VALUE;

    private void record(long value) {
      lock.writeLock().lock();
      try {
        count++;
        sum += value;
        min = Math.min(value, min);
        max = Math.max(value, max);
      } finally {
        lock.writeLock().unlock();
      }
    }

    private void mergeAndReset(LongSummary other) {
      long myCount;
      long mySum;
      long myMin;
      long myMax;
      lock.writeLock().lock();
      try {
        if (this.count == 0) {
          return;
        }
        myCount = this.count;
        mySum = this.sum;
        myMin = this.min;
        myMax = this.max;
        this.count = 0;
        this.sum = 0;
        this.min = Long.MAX_VALUE;
        this.max = Long.MIN_VALUE;
      } finally {
        lock.writeLock().unlock();
      }
      other.lock.writeLock().lock();
      try {
        other.count += myCount;
        other.sum += mySum;
        other.min = Math.min(myMin, other.min);
        other.max = Math.max(myMax, other.max);
      } finally {
        other.lock.writeLock().unlock();
      }
    }

    @Nullable
    private SummaryPoint toPoint(long startEpochNanos, long epochNanos, Labels labels) {
      lock.readLock().lock();
      try {
        return count == 0
            ? null
            : SummaryPoint.create(
                startEpochNanos,
                epochNanos,
                labels,
                count,
                sum,
                Arrays.asList(
                    ValueAtPercentile.create(0.0, min), ValueAtPercentile.create(100.0, max)));
      } finally {
        lock.readLock().unlock();
      }
    }
  }
}
