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
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.MutableMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
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
    implements Aggregator<ExponentialHistogramPointData, DoubleExemplarData> {

  private final Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier;
  private final int maxBuckets;
  private final int maxScale;
  private final MutableMetricData metricData =
      new MutableMetricData(MetricDataType.EXPONENTIAL_HISTOGRAM);
  private final MutableExponentialHistogramData data = new MutableExponentialHistogramData();

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
  public AggregatorHandle<ExponentialHistogramPointData, DoubleExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), maxBuckets, maxScale);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Collection<ExponentialHistogramPointData> points,
      AggregationTemporality temporality) {
    data.set(temporality, points);
    metricData.set(
        resource,
        instrumentationScopeInfo,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getSourceInstrument().getUnit(),
        data);
    return metricData;
  }

  static final class Handle
      extends AggregatorHandle<ExponentialHistogramPointData, DoubleExemplarData> {
    private final int maxBuckets;
    @Nullable private DoubleExponentialHistogramBuckets positiveBuckets;
    @Nullable private DoubleExponentialHistogramBuckets negativeBuckets;
    private long zeroCount;
    private double sum;
    private double min;
    private double max;
    private long count;
    private int scale;
    private final MutableExponentialHistogramPointData point =
        new MutableExponentialHistogramPointData();
    private final MutableExponentialHistogramBuckets mutablePositiveBuckets;
    private final MutableExponentialHistogramBuckets mutableNegativeBuckets;

    Handle(ExemplarReservoir<DoubleExemplarData> reservoir, int maxBuckets, int maxScale) {
      super(reservoir);
      this.maxBuckets = maxBuckets;
      this.sum = 0;
      this.zeroCount = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
      this.scale = maxScale;
      this.mutablePositiveBuckets = new MutableExponentialHistogramBuckets(maxBuckets);
      this.mutableNegativeBuckets = new MutableExponentialHistogramBuckets(maxBuckets);
    }

    @Override
    protected synchronized ExponentialHistogramPointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      point.set(
          scale,
          sum,
          zeroCount,
          this.count > 0,
          this.min,
          this.count > 0,
          this.max,
          resolveBuckets(this.positiveBuckets, this.mutablePositiveBuckets, scale, reset),
          resolveBuckets(this.negativeBuckets, this.mutableNegativeBuckets, scale, reset),
          startEpochNanos,
          epochNanos,
          attributes,
          exemplars);
      if (reset) {
        this.sum = 0;
        this.zeroCount = 0;
        this.min = Double.MAX_VALUE;
        this.max = -1;
        this.count = 0;
      }
      return point;
    }

    private static ExponentialHistogramBuckets resolveBuckets(
        @Nullable DoubleExponentialHistogramBuckets buckets,
        MutableExponentialHistogramBuckets mutableBuckets,
        int scale,
        boolean reset) {
      if (buckets == null) {
        return EmptyExponentialHistogramBuckets.get(scale);
      }
      mutableBuckets.set(buckets);
      if (reset) {
        buckets.clear();
      }
      return mutableBuckets;
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
      // unifying scale factor between positive/negative at collection time
      // (doAggregateThenMaybeReset).
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
