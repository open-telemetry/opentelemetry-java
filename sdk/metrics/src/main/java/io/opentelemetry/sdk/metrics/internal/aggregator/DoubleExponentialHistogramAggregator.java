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
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
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

  private final Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier;
  private final ExponentialBucketStrategy bucketStrategy;

  /**
   * Constructs an exponential histogram aggregator.
   *
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   */
  public DoubleExponentialHistogramAggregator(
      Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier, int maxBuckets) {
    this(
        reservoirSupplier,
        ExponentialBucketStrategy.newStrategy(
            maxBuckets, ExponentialCounterFactory.circularBufferCounter()));
  }

  DoubleExponentialHistogramAggregator(
      Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier,
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
   * @param previous the previously captured accumulation
   * @param current the newly captured (delta) accumulation
   * @return the result of the merge of the given accumulations.
   */
  @Override
  public ExponentialHistogramAccumulation merge(
      ExponentialHistogramAccumulation previous, ExponentialHistogramAccumulation current) {

    // Create merged buckets
    DoubleExponentialHistogramBuckets posBuckets =
        DoubleExponentialHistogramBuckets.merge(
            previous.getPositiveBuckets(), current.getPositiveBuckets());
    DoubleExponentialHistogramBuckets negBuckets =
        DoubleExponentialHistogramBuckets.merge(
            previous.getNegativeBuckets(), current.getNegativeBuckets());

    // resolve possible scale difference due to merge
    int commonScale = Math.min(posBuckets.getScale(), negBuckets.getScale());
    posBuckets.downscale(posBuckets.getScale() - commonScale);
    negBuckets.downscale(negBuckets.getScale() - commonScale);
    double min = -1;
    double max = -1;
    if (previous.hasMinMax() && current.hasMinMax()) {
      min = Math.min(previous.getMin(), current.getMin());
      max = Math.max(previous.getMax(), current.getMax());
    } else if (previous.hasMinMax()) {
      min = previous.getMin();
      max = previous.getMax();
    } else if (current.hasMinMax()) {
      min = current.getMin();
      max = current.getMax();
    }
    return ExponentialHistogramAccumulation.create(
        posBuckets.getScale(),
        previous.getSum() + current.getSum(),
        previous.hasMinMax() || current.hasMinMax(),
        min,
        max,
        posBuckets,
        negBuckets,
        previous.getZeroCount() + current.getZeroCount(),
        current.getExemplars());
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
    private double min;
    private double max;
    private long count;

    Handle(
        ExemplarReservoir<DoubleExemplarData> reservoir, ExponentialBucketStrategy bucketStrategy) {
      super(reservoir);
      this.sum = 0;
      this.zeroCount = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
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
              this.count > 0,
              this.count > 0 ? this.min : -1,
              this.count > 0 ? this.max : -1,
              positiveBuckets.copy(),
              negativeBuckets.copy(),
              zeroCount,
              exemplars);
      this.sum = 0;
      this.zeroCount = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
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

      this.min = Math.min(this.min, value);
      this.max = Math.max(this.max, value);
      count++;

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
