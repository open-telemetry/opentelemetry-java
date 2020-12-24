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
public final class LongMinMaxSumCountAggregator extends Aggregator<MinMaxSumCountAccumulation> {
  private static final AggregatorFactory<MinMaxSumCountAccumulation> AGGREGATOR_FACTORY =
      new AggregatorFactory<MinMaxSumCountAccumulation>() {
        @Override
        public Aggregator<MinMaxSumCountAccumulation> getAggregator() {
          return new LongMinMaxSumCountAggregator();
        }

        @Override
        public MinMaxSumCountAccumulation accumulateLong(long value) {
          return MinMaxSumCountAccumulation.create(1, value, value, value);
        }
      };

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  // The current value. This controls its own internal thread-safety via method access. Don't
  // try to use its fields directly.
  @GuardedBy("lock")
  private final LongState current = new LongState();

  private LongMinMaxSumCountAggregator() {}

  public static AggregatorFactory<MinMaxSumCountAccumulation> getFactory() {
    return AGGREGATOR_FACTORY;
  }

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
  protected void doRecordLong(long value) {
    lock.writeLock().lock();
    try {
      current.record(value);
    } finally {
      lock.writeLock().unlock();
    }
  }

  private static final class LongState {
    private long count;
    private long sum;
    private long min;
    private long max;

    public LongState() {
      reset();
    }

    private void reset() {
      this.sum = 0;
      this.count = 0;
      this.min = Long.MAX_VALUE;
      this.max = Long.MIN_VALUE;
    }

    public void record(long value) {
      count++;
      sum += value;
      min = Math.min(value, min);
      max = Math.max(value, max);
    }
  }
}
