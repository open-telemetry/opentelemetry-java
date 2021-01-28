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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class DoubleHistogramAggregator extends AbstractAggregator<HistogramAccumulation> {
  private static final Logger logger = Logger.getLogger(DoubleHistogramAggregator.class.getName());

  private static volatile boolean loggedMergingInvalidBoundaries = false;

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

  private static List<Double> asUnmodifiableDoubleList(double[] xs) {
    List<Double> result = new ArrayList<>(xs.length);
    for (double x : xs) {
      result.add(x);
    }
    return result;
  }

  private static List<Long> asUnmodifiableLongList(long[] xs) {
    List<Long> result = new ArrayList<>(xs.length);
    for (long x : xs) {
      result.add(x);
    }
    return result;
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
          0, 0, Collections.emptyList(), Collections.singletonList(0L));
    }

    long[] mergedCounts = new long[x.getCounts().size()];
    for (int i = 0; i < x.getCounts().size(); ++i) {
      mergedCounts[i] = x.getCounts().get(i) + y.getCounts().get(i);
    }
    return HistogramAccumulation.create(
        x.getCount() + y.getCount(),
        x.getSum() + y.getSum(),
        x.getBoundaries(),
        asUnmodifiableLongList(mergedCounts));
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
      double sum;
      List<Long> counts;
      lock.writeLock().lock();
      try {
        sum = current.sum;
        counts = asUnmodifiableLongList(current.counts);
        current.reset();
      } finally {
        lock.writeLock().unlock();
      }

      return HistogramAccumulation.create(
          counts.stream().mapToLong(i -> i).sum(),
          sum,
          asUnmodifiableDoubleList(current.boundaries),
          counts);
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
      private double sum;
      private final double[] boundaries;
      private final long[] counts;

      public State(double[] boundaries) {
        this.boundaries = Arrays.copyOf(boundaries, boundaries.length);
        this.counts = new long[this.boundaries.length + 1];
        reset();
      }

      // Benchmark shows that linear search performs better than binary search with ordinary
      // buckets.
      private int findBucketIndex(double value) {
        for (int i = 0; i < this.boundaries.length; ++i) {
          if (value < this.boundaries[i]) {
            return i;
          }
        }
        return this.boundaries.length;
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
