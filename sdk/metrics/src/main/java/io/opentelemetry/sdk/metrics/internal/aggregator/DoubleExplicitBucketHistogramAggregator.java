/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.PrimitiveLongList;
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
import java.util.concurrent.locks.ReentrantLock;
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

    private final Cell[] cells;
    private final long[] countsArr;

    // Used only when MemoryMode = REUSABLE_DATA
    @Nullable private final MutableHistogramPointData reusablePoint;

    Handle(
        List<Double> boundaryList,
        double[] boundaries,
        ExemplarReservoirFactory reservoirFactory,
        MemoryMode memoryMode) {
      super(reservoirFactory, /* isDoubleType= */ true);
      this.boundaryList = boundaryList;
      this.boundaries = boundaries;
      this.cells = new Cell[Runtime.getRuntime().availableProcessors()];
      for (int i = 0; i < cells.length; i++) {
        cells[i] = new Cell(boundaries.length + 1);
      }
      this.countsArr = new long[boundaries.length + 1];
      if (memoryMode == MemoryMode.REUSABLE_DATA) {
        this.reusablePoint = new MutableHistogramPointData(countsArr.length);
      } else {
        this.reusablePoint = null;
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
    protected HistogramPointData doAggregateThenMaybeResetDoubles(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      for (Cell cell : cells) {
        cell.lock.lock();
      }
      try {
        HistogramPointData pointData;
        Arrays.fill(countsArr, 0);
        double sum = 0;
        long count = 0;
        double min = Double.MAX_VALUE;
        double max = -1;

        for (Cell cell : cells) {
          sum += cell.sum;
          min = Math.min(min, cell.min);
          max = Math.max(max, cell.max);
          for (int i = 0; i < cell.counts.length; i++) {
            long currentCellCount = cell.counts[i];
            count += currentCellCount;
            countsArr[i] += currentCellCount;
          }
          if (reset) {
            cell.sum = 0;
            cell.min = Double.MAX_VALUE;
            cell.max = -1;
            Arrays.fill(cell.counts, 0);
          }
        }
        if (reusablePoint == null) {
          pointData =
              ImmutableHistogramPointData.create(
                  startEpochNanos,
                  epochNanos,
                  attributes,
                  sum,
                  count > 0,
                  min,
                  count > 0,
                  max,
                  boundaryList,
                  PrimitiveLongList.wrap(Arrays.copyOf(countsArr, countsArr.length)),
                  exemplars);
        } else /* REUSABLE_DATA */ {
          pointData =
              reusablePoint.set(
                  startEpochNanos,
                  epochNanos,
                  attributes,
                  sum,
                  count > 0,
                  min,
                  count > 0,
                  max,
                  boundaryList,
                  countsArr,
                  exemplars);
        }
        return pointData;
      } finally {
        for (Cell cell : cells) {
          cell.lock.unlock();
        }
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      int bucketIndex = ExplicitBucketHistogramUtils.findBucketIndex(this.boundaries, value);

      int cellIndex = Math.abs((int) (Thread.currentThread().getId() % cells.length));
      Cell cell = cells[cellIndex];
      cell.lock.lock();
      try {
        cell.sum += value;
        cell.min = Math.min(cell.min, value);
        cell.max = Math.max(cell.max, value);
        cell.counts[bucketIndex]++;
      } finally {
        cell.lock.unlock();
      }
    }

    private static class Cell {
      private final ReentrantLock lock = new ReentrantLock();
      private final long[] counts;
      private double sum = 0;
      private double min = Double.MAX_VALUE;
      private double max = -1;

      private Cell(int buckets) {
        this.counts = new long[buckets];
      }
    }
  }
}
