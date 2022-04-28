/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Aggregator that generates exponential histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleExponentialHistogramAggregator
    implements Aggregator<ExponentialHistogramAccumulation, DoubleExemplarData> {

  private final Supplier<DoubleExemplarReservoir> reservoirSupplier;
  private final ExponentialBucketStrategy bucketStrategy;

  /**
   * Constructs an exponential histogram aggregator.
   *
   * @param scale the starting scale.
   * @param maxBuckets the maximum number of buckets that will be used for positive or negative
   *     recordings.
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   */
  public DoubleExponentialHistogramAggregator(
      Supplier<DoubleExemplarReservoir> reservoirSupplier, int scale, int maxBuckets) {
    this(
        reservoirSupplier,
        ExponentialBucketStrategy.newStrategy(
            scale, maxBuckets, ExponentialCounterFactory.circularBufferCounter()));
  }

  DoubleExponentialHistogramAggregator(
      Supplier<DoubleExemplarReservoir> reservoirSupplier,
      ExponentialBucketStrategy bucketStrategy) {
    this.reservoirSupplier = reservoirSupplier;
    this.bucketStrategy = bucketStrategy;
  }

  @Override
  public AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), this.bucketStrategy);
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
   * <p>It is similar to merge(), however it decrements counts and sum instead of incrementing. It
   * does not modify the accumulations.
   *
   * @param previousCumulative the previously captured accumulation.
   * @param currentCumulative the newly captured (cumulative) accumulation.
   * @return The resulting delta accumulation.
   */
  @Override
  public ExponentialHistogramAccumulation diff(
      ExponentialHistogramAccumulation previousCumulative,
      ExponentialHistogramAccumulation currentCumulative) {

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
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Map<Attributes, ExponentialHistogramAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return ImmutableMetricData.createExponentialHistogram(
        resource,
        instrumentationScopeInfo,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getSourceInstrument().getUnit(),
        ExponentialHistogramData.create(
            temporality,
            MetricDataUtils.toExponentialHistogramPointList(
                accumulationByLabels,
                (temporality == AggregationTemporality.CUMULATIVE)
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
  }

  static final class Handle
      extends AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> {
    private final DoubleExponentialHistogramBuckets positiveBuckets;
    private final DoubleExponentialHistogramBuckets negativeBuckets;
    private long zeroCount;
    private double sum;

    Handle(DoubleExemplarReservoir reservoir, ExponentialBucketStrategy bucketStrategy) {
      super(reservoir);
      this.sum = 0;
      this.zeroCount = 0;
      this.positiveBuckets = bucketStrategy.newBuckets();
      this.negativeBuckets = bucketStrategy.newBuckets();
    }

    @Override
    protected synchronized ExponentialHistogramAccumulation doAccumulateThenReset(
        List<DoubleExemplarData> exemplars) {
      ExponentialHistogramAccumulation acc =
          ExponentialHistogramAccumulation.create(
              this.positiveBuckets.getScale(),
              sum,
              positiveBuckets.copy(),
              negativeBuckets.copy(),
              zeroCount,
              exemplars);
      this.sum = 0;
      this.zeroCount = 0;
      this.positiveBuckets.clear();
      this.negativeBuckets.clear();
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
      // TODO: We should experiment with downscale on demand during sync execution and only
      // unifying scale factor between positive/negative at collection time (doAccumulate).
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
    }
  }
}
