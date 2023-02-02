/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Aggregator that generates exponential histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleExponentialHistogramAggregator
    implements Aggregator<ExponentialHistogramAccumulation, DoubleExemplarData> {

  private final Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier;
  private final int maxBuckets;
  private final int maxScale;

  /**
   * Constructs an exponential histogram aggregator.
   *
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   */
  public DoubleExponentialHistogramAggregator(
      Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier,
      int maxBuckets,
      int maxScale) {
    this.reservoirSupplier = reservoirSupplier;
    this.maxBuckets = maxBuckets;
    this.maxScale = maxScale;
  }

  @Override
  public AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), maxBuckets, maxScale);
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
        ImmutableExponentialHistogramData.create(
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
    private final int maxBuckets;
    @Nullable private DoubleExponentialHistogramBuckets positiveBuckets;
    @Nullable private DoubleExponentialHistogramBuckets negativeBuckets;
    private long zeroCount;
    private double sum;
    private double min;
    private double max;
    private long count;
    private int scale;

    Handle(ExemplarReservoir<DoubleExemplarData> reservoir, int maxBuckets, int maxScale) {
      super(reservoir);
      this.maxBuckets = maxBuckets;
      this.sum = 0;
      this.zeroCount = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
      this.scale = maxScale;
    }

    @Override
    protected synchronized ExponentialHistogramAccumulation doAccumulateThenMaybeReset(
        List<DoubleExemplarData> exemplars, boolean reset) {
      ExponentialHistogramAccumulation acc =
          ExponentialHistogramAccumulation.create(
              scale,
              sum,
              this.count > 0,
              this.count > 0 ? this.min : -1,
              this.count > 0 ? this.max : -1,
              resolveBuckets(this.positiveBuckets, scale, reset),
              resolveBuckets(this.negativeBuckets, scale, reset),
              zeroCount,
              exemplars);
      if (reset) {
        this.sum = 0;
        this.zeroCount = 0;
        this.min = Double.MAX_VALUE;
        this.max = -1;
        this.count = 0;
      }
      return acc;
    }

    private static ExponentialHistogramBuckets resolveBuckets(
        @Nullable DoubleExponentialHistogramBuckets buckets, int scale, boolean reset) {
      if (buckets == null) {
        return EmptyExponentialHistogramBuckets.get(scale);
      }
      ExponentialHistogramBuckets copy = buckets.copy();
      if (reset) {
        buckets.clear();
      }
      return copy;
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
      DoubleExponentialHistogramBuckets buckets;
      if (c == 0) {
        zeroCount++;
        return;
      } else if (c > 0) {
        // Initialize positive buckets at current scale, if needed
        if (positiveBuckets == null) {
          positiveBuckets = new DoubleExponentialHistogramBuckets(scale, maxBuckets);
        }
        buckets = positiveBuckets;
      } else {
        // Initialize negative buckets at current scale, if needed
        if (negativeBuckets == null) {
          negativeBuckets = new DoubleExponentialHistogramBuckets(scale, maxBuckets);
        }
        buckets = negativeBuckets;
      }

      // Record; If recording fails, calculate scale reduction and scale down to fit new value.
      // 2nd attempt at recording should work with new scale
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
      if (positiveBuckets != null) {
        positiveBuckets.downscale(by);
        scale = positiveBuckets.getScale();
      }
      if (negativeBuckets != null) {
        negativeBuckets.downscale(by);
        scale = negativeBuckets.getScale();
      }
    }
  }

  @AutoValue
  abstract static class EmptyExponentialHistogramBuckets implements ExponentialHistogramBuckets {

    private static final Map<Integer, ExponentialHistogramBuckets> ZERO_BUCKETS =
        new ConcurrentHashMap<>();

    EmptyExponentialHistogramBuckets() {}

    static ExponentialHistogramBuckets get(int scale) {
      return ZERO_BUCKETS.computeIfAbsent(
          scale,
          scale1 ->
              new AutoValue_DoubleExponentialHistogramAggregator_EmptyExponentialHistogramBuckets(
                  scale1, 0, Collections.emptyList(), 0));
    }
  }
}
