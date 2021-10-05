/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

final class DoubleExponentialHistogramAggregator
    extends AbstractAggregator<ExponentialHistogramAccumulation> {

  private final int scale;
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  DoubleExponentialHistogramAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor metricDescriptor,
      boolean stateful,
      int scale,
      Supplier<ExemplarReservoir> reservoirSupplier) {

    super(resource, instrumentationLibraryInfo, metricDescriptor, stateful);
    this.scale = scale;
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<ExponentialHistogramAccumulation> createHandle() {
    return new Handle(scale, reservoirSupplier.get());
  }

  @Override
  public ExponentialHistogramAccumulation accumulateLong(long value) {
    return accumulateDouble((double) value);
  }

  @Override
  public ExponentialHistogramAccumulation accumulateDouble(double value) {
    long zeroCount = 0;
    DoubleExponentialHistogramBuckets positiveBuckets =
        new DoubleExponentialHistogramBuckets(scale);
    DoubleExponentialHistogramBuckets negativeBuckets =
        new DoubleExponentialHistogramBuckets(scale);
    if (Double.isFinite(value)) {
      int c = Double.compare(value, 0);
      if (c == 0) {
        zeroCount++;
      } else if (c > 0) {
        positiveBuckets.record(value);
      } else /* c < 0 */ {
        negativeBuckets.record(value);
      }
    }
    return ExponentialHistogramAccumulation.create(
        this.scale, value, positiveBuckets, negativeBuckets, zeroCount);
  }

  @Override
  public ExponentialHistogramAccumulation merge(
      ExponentialHistogramAccumulation previousAccumulation,
      ExponentialHistogramAccumulation accumulation) {
    // todo
    return null;
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, ExponentialHistogramAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleExponentialHistogram(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
        getMetricDescriptor().getUnit(),
        new DoubleExponentialHistogram(
            this.isStateful() ? AggregationTemporality.CUMULATIVE : AggregationTemporality.DELTA,
            MetricDataUtils.toExponentialHistogramPointList(
                accumulationByLabels, startEpochNanos, epochNanos)));
  }

  static final class Handle extends AggregatorHandle<ExponentialHistogramAccumulation> {
    // todo will need lock for any mutable values

    private final int scale;
    private DoubleExponentialHistogramBuckets positiveBuckets;
    private DoubleExponentialHistogramBuckets negativeBuckets;
    private long zeroCount;
    private double sum;

    Handle(int scale, ExemplarReservoir reservoir) {
      super(reservoir);
      this.sum = 0;
      this.zeroCount = 0;
      this.scale = scale;
      this.positiveBuckets = new DoubleExponentialHistogramBuckets(scale);
      this.negativeBuckets = new DoubleExponentialHistogramBuckets(scale);
    }

    @Override
    protected ExponentialHistogramAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      ExponentialHistogramAccumulation acc =
          ExponentialHistogramAccumulation.create(
              scale, sum, positiveBuckets, negativeBuckets, zeroCount, exemplars);
      this.sum = 0;
      this.zeroCount = 0;
      this.positiveBuckets = new DoubleExponentialHistogramBuckets(scale);
      this.negativeBuckets = new DoubleExponentialHistogramBuckets(scale);
      return acc;
    }

    @Override
    protected void doRecordDouble(double value) {
      // todo review double comparisons (Double.compare()?)
      if (Double.isFinite(value)) {
        sum += value;
        int c = Double.compare(value, 0);
        if (c == 0) {
          zeroCount++;
        } else if (c > 0) {
          positiveBuckets.record(value);
        } else /* c < 0 */ {
          negativeBuckets.record(value);
        }
      }
    }

    @Override
    protected void doRecordLong(long value) {
      doRecordDouble((double) value);
    }
  }
}
