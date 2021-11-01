/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

final class DoubleExponentialHistogramAggregator
    implements Aggregator<ExponentialHistogramAccumulation> {

  private final Supplier<ExemplarReservoir> reservoirSupplier;

  DoubleExponentialHistogramAggregator(Supplier<ExemplarReservoir> reservoirSupplier) {
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<ExponentialHistogramAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  /**
   * This function is an immutable merge. It firstly combines the sum and zero count. Then it
   * performs a merge using the buckets from both accumulations, without modifying those
   * accumulations.
   *
   * @param previousAccumulation the previously captured accumulation
   * @param delta the newly captured (delta) accumulation
   * @return the result of the merge of the given accumulations.
   */
  @Override
  public ExponentialHistogramAccumulation merge(
      ExponentialHistogramAccumulation previousAccumulation,
      ExponentialHistogramAccumulation delta) {

    double sum = previousAccumulation.getSum() + delta.getSum();
    long zeroCount = previousAccumulation.getZeroCount() + delta.getZeroCount();

    // Create merged buckets
    DoubleExponentialHistogramBuckets posBuckets =
        DoubleExponentialHistogramBuckets.merge(
            previousAccumulation.getPositiveBuckets(), delta.getPositiveBuckets());
    DoubleExponentialHistogramBuckets negBuckets =
        DoubleExponentialHistogramBuckets.merge(
            previousAccumulation.getNegativeBuckets(), delta.getNegativeBuckets());

    // resolve possible scale difference due to merge
    int commonScale = Math.min(posBuckets.getScale(), negBuckets.getScale());
    posBuckets.downscale(posBuckets.getScale() - commonScale);
    negBuckets.downscale(negBuckets.getScale() - commonScale);

    return ExponentialHistogramAccumulation.create(
        posBuckets.getScale(), sum, posBuckets, negBuckets, zeroCount, delta.getExemplars());
  }

  /**
   * Returns a new DELTA aggregation by comparing two cumulative measurements.
   *
   * @param previousCumulative the previously captured accumulation.
   * @param currentCumulative the newly captured (cumulative) accumulation.
   * @return The resulting delta accumulation.
   */
  @Override
  public ExponentialHistogramAccumulation diff(
      ExponentialHistogramAccumulation previousCumulative,
      ExponentialHistogramAccumulation currentCumulative) {

    // or maybe just do something similar to merge instead minus rather than increment?
    double sum = currentCumulative.getSum() - previousCumulative.getSum();
    long zeroCount = currentCumulative.getZeroCount() - previousCumulative.getZeroCount();

    DoubleExponentialHistogramBuckets posBuckets =
        DoubleExponentialHistogramBuckets.diff(
            currentCumulative.getPositiveBuckets(), previousCumulative.getPositiveBuckets());
    DoubleExponentialHistogramBuckets negBuckets =
        DoubleExponentialHistogramBuckets.diff(
            currentCumulative.getNegativeBuckets(), previousCumulative.getNegativeBuckets());

    // resolve possible scale difference due to merge
    int commonScale = Math.min(posBuckets.getScale(), negBuckets.getScale());
    posBuckets.downscale(posBuckets.getScale() - commonScale);
    negBuckets.downscale(negBuckets.getScale() - commonScale);

    return ExponentialHistogramAccumulation.create(
        posBuckets.getScale(),
        sum,
        posBuckets,
        negBuckets,
        zeroCount,
        currentCumulative.getExemplars());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibrary,
      MetricDescriptor metricDescriptor,
      Map<Attributes, ExponentialHistogramAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createExponentialHistogram(
        resource,
        instrumentationLibrary,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getUnit(),
        ExponentialHistogramData.create(
            temporality,
            MetricDataUtils.toExponentialHistogramPointList(
                accumulationByLabels,
                (temporality == AggregationTemporality.CUMULATIVE)
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
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
    protected synchronized ExponentialHistogramAccumulation doAccumulateThenReset(
        List<ExemplarData> exemplars) {
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
