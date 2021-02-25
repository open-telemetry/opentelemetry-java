/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.concurrent.GuardedBy;

final class DoubleHistogramAggregator extends AbstractAggregator<HistogramAccumulation> {
  private static final long[] countsOfOne = new long[] {1};

  private final double[] boundaries;

  // a cache for converting to MetricData
  private final List<Double> boundaryList;

  DoubleHistogramAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      double[] boundaries,
      boolean stateful) {
    super(resource, instrumentationLibraryInfo, instrumentDescriptor, stateful);
    this.boundaries = boundaries;

    List<Double> boundaryList = new ArrayList<>(this.boundaries.length);
    for (double v : this.boundaries) {
      boundaryList.add(v);
    }
    this.boundaryList = Collections.unmodifiableList(boundaryList);
  }

  @Override
  public AggregatorHandle<HistogramAccumulation> createHandle() {
    return new Handle(this.boundaries);
  }

  /**
   * Return the result of the merge of two histogram accumulations. As long as one Aggregator
   * instance produces all Accumulations with constant boundaries we don't need to worry about
   * merging accumulations with different boundaries.
   */
  @Override
  public final HistogramAccumulation merge(HistogramAccumulation x, HistogramAccumulation y) {
    long[] mergedCounts = new long[x.getCounts().length];
    for (int i = 0; i < x.getCounts().length; ++i) {
      mergedCounts[i] = x.getCounts()[i] + y.getCounts()[i];
    }
    return HistogramAccumulation.create(x.getSum() + y.getSum(), mergedCounts);
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
                epochNanos,
                boundaryList)));
  }

  @Override
  public HistogramAccumulation accumulateDouble(double value) {
    return HistogramAccumulation.create(value, countsOfOne);
  }

  @Override
  public HistogramAccumulation accumulateLong(long value) {
    return HistogramAccumulation.create(value, countsOfOne);
  }

  static final class Handle extends AggregatorHandle<HistogramAccumulation> {
    private final double[] boundaries;

    private final ReentrantLock lock = new ReentrantLock();

    @GuardedBy("lock")
    private final State current;

    Handle(double[] boundaries) {
      this.boundaries = boundaries;
      this.current = new State(this.boundaries.length + 1);
    }

    // Benchmark shows that linear search performs better than binary search with ordinary
    // buckets.
    private int findBucketIndex(double value) {
      for (int i = 0; i < boundaries.length; ++i) {
        if (Double.compare(value, boundaries[i]) <= 0) {
          return i;
        }
      }
      return boundaries.length;
    }

    @Override
    protected HistogramAccumulation doAccumulateThenReset() {
      lock.lock();
      try {
        HistogramAccumulation acc =
            HistogramAccumulation.create(
                current.sum, Arrays.copyOf(current.counts, current.counts.length));
        current.reset();
        return acc;
      } finally {
        lock.unlock();
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      int bucketIndex = findBucketIndex(value);

      lock.lock();
      try {
        current.record(bucketIndex, value);
      } finally {
        lock.unlock();
      }
    }

    @Override
    protected void doRecordLong(long value) {
      doRecordDouble((double) value);
    }

    private static final class State {
      private double sum;
      private final long[] counts;

      public State(int bucketSize) {
        this.counts = new long[bucketSize];
        reset();
      }

      private void reset() {
        this.sum = 0;
        Arrays.fill(this.counts, 0);
      }

      private void record(int bucketIndex, double value) {
        this.sum += value;
        this.counts[bucketIndex]++;
      }
    }
  }
}
