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

  private final Supplier<ExemplarReservoir> reservoirSupplier;

  DoubleExponentialHistogramAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor metricDescriptor,
      boolean stateful,
      Supplier<ExemplarReservoir> reservoirSupplier) {

    super(resource, instrumentationLibraryInfo, metricDescriptor, stateful);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<ExponentialHistogramAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public ExponentialHistogramAccumulation accumulateLong(long value) {
    return accumulateDouble((double) value);
  }

  @Override
  public ExponentialHistogramAccumulation accumulateDouble(double value) {
    AggregatorHandle<ExponentialHistogramAccumulation> handle = this.createHandle();
    handle.recordDouble(value);
    return handle.accumulateThenReset(Attributes.empty());
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
    return MetricData.createExponentialHistogram(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
        getMetricDescriptor().getUnit(),
        DoubleExponentialHistogramData.create(
            this.isStateful() ? AggregationTemporality.CUMULATIVE : AggregationTemporality.DELTA,
            MetricDataUtils.toExponentialHistogramPointList(
                accumulationByLabels, startEpochNanos, epochNanos)));
  }

  static final class Handle extends AggregatorHandle<ExponentialHistogramAccumulation> {

    private int scale;
    private DoubleExponentialHistogramBuckets positiveBuckets;
    private DoubleExponentialHistogramBuckets negativeBuckets;
    private long zeroCount;
    private double sum;

    Handle(ExemplarReservoir reservoir) {
      super(reservoir);
      this.sum = 0;
      this.zeroCount = 0;
      this.scale = DoubleExponentialHistogramBuckets.MAX_SCALE;
      this.positiveBuckets = new DoubleExponentialHistogramBuckets();
      this.negativeBuckets = new DoubleExponentialHistogramBuckets();
    }

    @Override
    protected synchronized ExponentialHistogramAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      ExponentialHistogramAccumulation acc =
          ExponentialHistogramAccumulation.create(
              scale, sum, positiveBuckets, negativeBuckets, zeroCount, exemplars);
      this.sum = 0;
      this.zeroCount = 0;
      this.positiveBuckets = new DoubleExponentialHistogramBuckets();
      this.negativeBuckets = new DoubleExponentialHistogramBuckets();
      return acc;
    }

    @Override
    protected synchronized void doRecordDouble(double value) {

      // ignore NaN and infinity
      if (!Double.isFinite(value)) {
        return;
      }

      sum += value;
      int c = Double.compare(value, 0);

      if (c == 0) {
        zeroCount++;
        return;
      }

      // Record; If recording fails, calculate scale reduction and scale down to fit new value.
      // 2nd attempt at recording should work with new scale
      DoubleExponentialHistogramBuckets buckets = (c > 0) ? positiveBuckets : negativeBuckets;
      if (!buckets.record(value)) {
        // getScaleReduction() used with downScale() will scale down as required to record value,
        // fit inside max allowed buckets, and make sure index can be represented by int.
        downScale(buckets.getScaleReduction(value));
        buckets.record(value);
      }
    }

    @Override
    protected void doRecordLong(long value) {
      doRecordDouble((double) value);
    }

    void downScale(int by) {
      positiveBuckets.downscale(by);
      negativeBuckets.downscale(by);
      this.scale -= by;
    }
  }
}
