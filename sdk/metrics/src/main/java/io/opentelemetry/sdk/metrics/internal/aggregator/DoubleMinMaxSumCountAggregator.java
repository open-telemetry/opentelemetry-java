/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Aggregator that produces summary metrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
final class DoubleMinMaxSumCountAggregator extends AbstractMinMaxSumCountAggregator {
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  public DoubleMinMaxSumCountAggregator(Supplier<ExemplarReservoir> reservoirSupplier) {
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<MinMaxSumCountAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  static final class Handle extends AggregatorHandle<MinMaxSumCountAccumulation> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    // The current value. This controls its own internal thread-safety via method access. Don't
    // try to use its fields directly.
    @GuardedBy("lock")
    private final DoubleState current = new DoubleState();

    public Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected MinMaxSumCountAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
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
}
