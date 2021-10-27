/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Aggregator that generates histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleHistogramAggregator implements Aggregator<HistogramAccumulation> {
  private final double[] boundaries;

  // a cache for converting to MetricData
  private final List<Double> boundaryList;

  private final Supplier<ExemplarReservoir> reservoirSupplier;

  /**
   * Constructs a histogram aggregator.
   *
   * @param boundaries Bucket boundaries, in-order.
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   */
  public DoubleHistogramAggregator(
      double[] boundaries, Supplier<ExemplarReservoir> reservoirSupplier) {
    this.boundaries = boundaries;

    List<Double> boundaryList = new ArrayList<>(this.boundaries.length);
    for (double v : this.boundaries) {
      boundaryList.add(v);
    }
    this.boundaryList = Collections.unmodifiableList(boundaryList);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<HistogramAccumulation> createHandle() {
    return new Handle(this.boundaries, reservoirSupplier.get());
  }

  /**
   * Return the result of the merge of two histogram accumulations. As long as one Aggregator
   * instance produces all Accumulations with constant boundaries we don't need to worry about
   * merging accumulations with different boundaries.
   */
  @Override
  public HistogramAccumulation merge(
      HistogramAccumulation previous, HistogramAccumulation current) {
    long[] previousCounts = previous.getCounts();
    long[] mergedCounts = new long[previousCounts.length];
    for (int i = 0; i < previousCounts.length; ++i) {
      mergedCounts[i] = previousCounts[i] + current.getCounts()[i];
    }
    return HistogramAccumulation.create(
        previous.getSum() + current.getSum(), mergedCounts, current.getExemplars());
  }

  @Override
  public HistogramAccumulation diff(HistogramAccumulation previous, HistogramAccumulation current) {
    long[] previousCounts = previous.getCounts();
    long[] diffedCounts = new long[previousCounts.length];
    for (int i = 0; i < previousCounts.length; ++i) {
      diffedCounts[i] = current.getCounts()[i] - previousCounts[i];
    }
    return HistogramAccumulation.create(
        current.getSum() - previous.getSum(), diffedCounts, current.getExemplars());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor metricDescriptor,
      Map<Attributes, HistogramAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleHistogram(
        resource,
        instrumentationLibraryInfo,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getUnit(),
        DoubleHistogramData.create(
            temporality,
            MetricDataUtils.toDoubleHistogramPointList(
                accumulationByLabels,
                (temporality == AggregationTemporality.CUMULATIVE)
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos,
                boundaryList)));
  }

  static final class Handle extends AggregatorHandle<HistogramAccumulation> {
    // read-only
    private final double[] boundaries;

    @GuardedBy("lock")
    private double sum;

    @GuardedBy("lock")
    private final long[] counts;

    private final ReentrantLock lock = new ReentrantLock();

    Handle(double[] boundaries, ExemplarReservoir reservoir) {
      super(reservoir);
      this.boundaries = boundaries;
      this.counts = new long[this.boundaries.length + 1];
      this.sum = 0;
    }

    @Override
    protected HistogramAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      lock.lock();
      try {
        HistogramAccumulation acc =
            HistogramAccumulation.create(sum, Arrays.copyOf(counts, counts.length), exemplars);
        this.sum = 0;
        Arrays.fill(this.counts, 0);
        return acc;
      } finally {
        lock.unlock();
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      int bucketIndex = ExplicitBucketHistogramUtils.findBucketIndex(this.boundaries, value);

      lock.lock();
      try {
        this.sum += value;
        this.counts[bucketIndex]++;
      } finally {
        lock.unlock();
      }
    }

    @Override
    protected void doRecordLong(long value) {
      doRecordDouble((double) value);
    }
  }
}
