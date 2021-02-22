/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.ImmutableDoubleArray;
import io.opentelemetry.sdk.metrics.common.ImmutableLongArray;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class DoubleHistogramAggregator extends AbstractAggregator<HistogramAccumulation> {
  private static final Logger logger = Logger.getLogger(DoubleHistogramAggregator.class.getName());

  private static volatile boolean loggedMergingInvalidBoundaries = false;

  private final ImmutableDoubleArray boundaries;

  DoubleHistogramAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      ImmutableDoubleArray boundaries,
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
      // If this happens, it's a pretty severe bug in the SDK.
      if (!loggedMergingInvalidBoundaries) {
        logger.log(
            Level.SEVERE,
            "can't merge histograms with different boundaries, something's very wrong: "
                + "x.boundaries="
                + x.getBoundaries()
                + " y.boundaries="
                + y.getBoundaries());
        loggedMergingInvalidBoundaries = true;
      }
      return HistogramAccumulation.create(
          0, 0, ImmutableDoubleArray.copyOf(new double[0]), ImmutableLongArray.of(0));
    }

    long[] mergedCounts = new long[x.getCounts().length()];
    for (int i = 0; i < x.getCounts().length(); ++i) {
      mergedCounts[i] = x.getCounts().get(i) + y.getCounts().get(i);
    }
    return HistogramAccumulation.create(
        x.getCount() + y.getCount(),
        x.getSum() + y.getSum(),
        x.getBoundaries(),
        ImmutableLongArray.copyOf(mergedCounts));
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
        1, value, ImmutableDoubleArray.copyOf(new double[0]), ImmutableLongArray.of(1));
  }

  @Override
  public HistogramAccumulation accumulateLong(long value) {
    return HistogramAccumulation.create(
        1, value, ImmutableDoubleArray.copyOf(new double[0]), ImmutableLongArray.of(1));
  }

  static final class Handle extends AggregatorHandle<HistogramAccumulation> {
    private final ImmutableDoubleArray boundaries;

    private final ReentrantLock lock = new ReentrantLock();

    @GuardedBy("lock")
    private final State current;

    Handle(ImmutableDoubleArray boundaries) {
      this.boundaries = boundaries;
      this.current = new State(this.boundaries.length() + 1);
    }

    // Benchmark shows that linear search performs better than binary search with ordinary
    // buckets.
    private int findBucketIndex(double value) {
      for (int i = 0; i < boundaries.length(); ++i) {
        if (value < boundaries.get(i)) {
          return i;
        }
      }
      return boundaries.length();
    }

    @Override
    protected HistogramAccumulation doAccumulateThenReset() {
      double sum;
      ImmutableLongArray counts;
      lock.lock();
      try {
        sum = current.sum;
        counts = ImmutableLongArray.copyOf(current.counts);
        current.reset();
      } finally {
        lock.unlock();
      }

      long totalCount = 0;
      for (int i = 0; i < counts.length(); ++i) {
        totalCount += counts.get(i);
      }

      return HistogramAccumulation.create(totalCount, sum, boundaries, counts);
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
