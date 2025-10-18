/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.MutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Aggregator that generates explicit bucket histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleExplicitBucketHistogramAggregator
    implements Aggregator<HistogramPointData> {
  private final double[] boundaries;
  private final MemoryMode memoryMode;

  // a cache for converting to MetricData
  private final List<Double> boundaryList;

  private final ExemplarReservoirFactory reservoirFactory;

  /**
   * Constructs an explicit bucket histogram aggregator.
   *
   * @param boundaries Bucket boundaries, in-order.
   * @param reservoirFactory Supplier of exemplar reservoirs per-stream.
   * @param memoryMode The {@link MemoryMode} to use in this aggregator.
   */
  public DoubleExplicitBucketHistogramAggregator(
      double[] boundaries, ExemplarReservoirFactory reservoirFactory, MemoryMode memoryMode) {
    this.boundaries = boundaries;
    this.memoryMode = memoryMode;

    List<Double> boundaryList = new ArrayList<>(this.boundaries.length);
    for (double v : this.boundaries) {
      boundaryList.add(v);
    }
    this.boundaryList = Collections.unmodifiableList(boundaryList);
    this.reservoirFactory = reservoirFactory;
  }

  @Override
  public AggregatorHandle<HistogramPointData> createHandle() {
    return new Handle(boundaryList, boundaries, reservoirFactory, memoryMode);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Collection<HistogramPointData> pointData,
      AggregationTemporality temporality) {
    return ImmutableMetricData.createDoubleHistogram(
        resource,
        instrumentationScopeInfo,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getSourceInstrument().getUnit(),
        ImmutableHistogramData.create(temporality, pointData));
  }

  static final class Handle extends AggregatorHandle<HistogramPointData> {
    // read-only
    private final List<Double> boundaryList;
    // read-only
    private final double[] boundaries;

    private final Object lock = new Object();

    @GuardedBy("lock")
    private double sum;

    @GuardedBy("lock")
    private double min;

    @GuardedBy("lock")
    private double max;

    @GuardedBy("lock")
    private long count;

    @GuardedBy("lock")
    private final long[] counts;

    // Used only when MemoryMode = REUSABLE_DATA
    @Nullable private MutableHistogramPointData reusablePoint;

    Handle(
        List<Double> boundaryList,
        double[] boundaries,
        ExemplarReservoirFactory reservoirFactory,
        MemoryMode memoryMode) {
      super(reservoirFactory);
      this.boundaryList = boundaryList;
      this.boundaries = boundaries;
      this.counts = new long[this.boundaries.length + 1];
      this.sum = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
      if (memoryMode == MemoryMode.REUSABLE_DATA) {
        this.reusablePoint = new MutableHistogramPointData(counts.length);
      }
    }

    @Override
    public void recordLong(long value, Attributes attributes, Context context) {
      // Since there is no LongExplicitBucketHistogramAggregator and we need to support measurements
      // from LongHistogram, we redirect calls from #recordLong to #recordDouble. Without this, the
      // base AggregatorHandle implementation of #recordLong throws.
      super.recordDouble((double) value, attributes, context);
    }

    @Override
    protected boolean isDoubleType() {
      return true;
    }

    @Override
    protected HistogramPointData doAggregateThenMaybeResetDoubles(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      synchronized (lock) {
        HistogramPointData pointData;
        if (reusablePoint == null) {
          pointData =
              ImmutableHistogramPointData.create(
                  startEpochNanos,
                  epochNanos,
                  attributes,
                  sum,
                  this.count > 0,
                  this.min,
                  this.count > 0,
                  this.max,
                  boundaryList,
                  PrimitiveLongList.wrap(Arrays.copyOf(counts, counts.length)),
                  exemplars);
        } else /* REUSABLE_DATA */ {
          pointData =
              reusablePoint.set(
                  startEpochNanos,
                  epochNanos,
                  attributes,
                  sum,
                  this.count > 0,
                  this.min,
                  this.count > 0,
                  this.max,
                  boundaryList,
                  counts,
                  exemplars);
        }
        if (reset) {
          this.sum = 0;
          this.min = Double.MAX_VALUE;
          this.max = -1;
          this.count = 0;
          Arrays.fill(this.counts, 0);
        }
        return pointData;
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      int bucketIndex = ExplicitBucketHistogramUtils.findBucketIndex(this.boundaries, value);

      synchronized (lock) {
        this.sum += value;
        this.min = Math.min(this.min, value);
        this.max = Math.max(this.max, value);
        this.count++;
        this.counts[bucketIndex]++;
      }
    }
  }
}
