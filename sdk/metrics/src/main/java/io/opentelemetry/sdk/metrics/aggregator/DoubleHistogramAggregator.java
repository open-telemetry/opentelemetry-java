/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.annotation.concurrent.GuardedBy;

final class DoubleHistogramAggregator extends AbstractAggregator<HistogramAccumulation> {
  private final double[] boundaries;

  DoubleHistogramAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      double[] boundaries,
      boolean stateful) {
    super(resource, instrumentationLibraryInfo, instrumentDescriptor, stateful);
    this.boundaries = boundaries;
  }

  @Override
  public AggregatorHandle<HistogramAccumulation> createHandle() {
    return new Handle(this.boundaries);
  }

  @Override
  public final HistogramAccumulation merge(HistogramAccumulation x, HistogramAccumulation y) {
    if (!x.getBoundaries().equals(y.getBoundaries())) {
      throw new IllegalArgumentException("can't merge histograms with different boundaries");
    }

    long[] mergedCounts = new long[x.getCounts().size()];
    for (int i = 0; i < x.getCounts().size(); ++i) {
      mergedCounts[i] = x.getCounts().get(i) + y.getCounts().get(i);
    }
    return HistogramAccumulation.create(
        x.getCount() + y.getCount(),
        x.getSum() + y.getSum(),
        x.getBoundaries(),
        Arrays.stream(mergedCounts).boxed().collect(Collectors.toList()));
  }

  @Override
  public final MetricData toMetricData(
      Map<Labels, HistogramAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleHistogram(
        getResource(),
        getInstrumentationLibraryInfo(),
        getInstrumentDescriptor().getName(),
        getInstrumentDescriptor().getDescription(),
        getInstrumentDescriptor().getUnit(),
        DoubleHistogramData.create(
            isStateful() ? AggregationTemporality.CUMULATIVE : AggregationTemporality.DELTA,
            MetricDataUtils.toDoubleHistogramPointList(
                accumulationByLabels,
                isStateful() ? startEpochNanos : lastCollectionEpoch,
                epochNanos)));
  }

  @Override
  public HistogramAccumulation accumulateDouble(double value) {
    return HistogramAccumulation.create(
        1, value, Collections.emptyList(), Collections.singletonList(1L));
  }

  @Override
  public HistogramAccumulation accumulateLong(long value) {
    return HistogramAccumulation.create(
        1, value, Collections.emptyList(), Collections.singletonList(1L));
  }

  static final class Handle extends AggregatorHandle<HistogramAccumulation> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("lock")
    private final State current;

    Handle(double[] boundaries) {
      current = new State(boundaries);
    }

    @Override
    protected HistogramAccumulation doAccumulateThenReset() {
      lock.writeLock().lock();
      try {
        long[] cumulative = new long[current.counts.length];
        cumulative[0] = current.counts[0];
        for (int i = 1; i < current.counts.length; ++i) {
          cumulative[i] = cumulative[i - 1] + current.counts[i];
        }

        HistogramAccumulation result =
            HistogramAccumulation.create(
                current.count,
                current.sum,
                Arrays.stream(current.boundaries).boxed().collect(Collectors.toList()),
                Arrays.stream(cumulative).boxed().collect(Collectors.toList()));
        current.reset();
        return result;
      } finally {
        lock.writeLock().unlock();
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      int bucketIndex = current.findBucketIndex(value);

      lock.writeLock().lock();
      try {
        current.record(bucketIndex, value);
      } finally {
        lock.writeLock().unlock();
      }
    }

    @Override
    protected void doRecordLong(long value) {
      doRecordDouble((double) value);
    }

    private static final class State {
      private long count;
      private double sum;
      private final double[] boundaries;
      private final long[] counts;

      public State(double[] boundaries) {
        this.boundaries = Arrays.copyOf(boundaries, boundaries.length);
        this.counts = new long[this.boundaries.length + 1];
        reset();
      }

      // Benchmark shows that linear search performs better with ordinary buckets.
      private int findBucketIndex(double value) {
        for (int i = 0; i < this.boundaries.length; ++i) {
          if (value < this.boundaries[i]) {
            return i;
          }
        }
        return this.boundaries.length;
      }

      private void reset() {
        this.count = 0;
        this.sum = 0;
        Arrays.fill(this.counts, 0);
      }

      private void record(int bucketIndex, double value) {
        this.count++;
        this.sum += value;
        this.counts[bucketIndex]++;
      }
    }
  }
}
