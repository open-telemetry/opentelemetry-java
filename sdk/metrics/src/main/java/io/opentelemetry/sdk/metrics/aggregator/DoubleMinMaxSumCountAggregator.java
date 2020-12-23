/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import io.opentelemetry.sdk.metrics.aggregation.MinMaxSumCountAccumulation;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class DoubleMinMaxSumCountAggregator extends Aggregator<MinMaxSumCountAccumulation> {
  private static final AggregatorFactory<MinMaxSumCountAccumulation> AGGREGATOR_FACTORY =
      new AggregatorFactory<MinMaxSumCountAccumulation>() {
        @Override
        public Aggregator<MinMaxSumCountAccumulation> getAggregator() {
          return new DoubleMinMaxSumCountAggregator();
        }

        @Override
        public MinMaxSumCountAccumulation accumulateDouble(double value) {
          return MinMaxSumCountAccumulation.create(1, value, value, value);
        }
      };

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  // The current value. This controls its own internal thread-safety via method access. Don't
  // try to use its fields directly.
  @GuardedBy("lock")
  private final DoubleState current = new DoubleState();

  public static AggregatorFactory<MinMaxSumCountAccumulation> getFactory() {
    return AGGREGATOR_FACTORY;
  }

  private DoubleMinMaxSumCountAggregator() {}

  @Override
  protected MinMaxSumCountAccumulation doAccumulateThenReset() {
    lock.writeLock().lock();
    try {
      MinMaxSumCountAccumulation toReturn =
          MinMaxSumCountAccumulation.create(current.count, current.sum, current.min, current.max);
      current.reset();
      return toReturn;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  protected void doRecordDouble(double value) {
    lock.writeLock().lock();
    try {
      current.record(value);
    } finally {
      lock.writeLock().unlock();
    }
  }

  private static final class DoubleState {
    private long count;
    private double sum;
    private double min;
    private double max;

    public DoubleState() {
      reset();
    }

    private void reset() {
      this.sum = 0;
      this.count = 0;
      this.min = Double.POSITIVE_INFINITY;
      this.max = Double.NEGATIVE_INFINITY;
    }

    public void record(double value) {
      count++;
      sum += value;
      min = Math.min(value, min);
      max = Math.max(value, max);
    }
  }
}
