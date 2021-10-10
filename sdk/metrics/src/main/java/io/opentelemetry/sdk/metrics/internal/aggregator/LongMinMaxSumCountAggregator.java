/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class LongMinMaxSumCountAggregator extends AbstractMinMaxSumCountAggregator {
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  LongMinMaxSumCountAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    super(resource, instrumentationLibraryInfo, descriptor);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<MinMaxSumCountAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public MinMaxSumCountAccumulation accumulateLong(long value) {
    return MinMaxSumCountAccumulation.create(1, value, value, value);
  }

  static final class Handle extends AggregatorHandle<MinMaxSumCountAccumulation> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    // The current value. This controls its own internal thread-safety via method access. Don't
    // try to use its fields directly.
    @GuardedBy("lock")
    private final LongState current = new LongState();

    Handle(ExemplarReservoir exemplarReservoir) {
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
}
