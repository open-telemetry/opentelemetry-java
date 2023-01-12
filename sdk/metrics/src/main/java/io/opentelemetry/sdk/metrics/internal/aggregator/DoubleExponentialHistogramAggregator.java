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
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramData;
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

  /**
   * Merge the exponential histogram accumulations. Mutates the {@link
   * ExponentialHistogramAccumulation#getPositiveBuckets()} and {@link
   * ExponentialHistogramAccumulation#getNegativeBuckets()} of {@code previous}. Mutating buckets is
   * acceptable because copies are already made in {@link Handle#doAccumulateThenReset(List)}.
   */
  @Override
  public ExponentialHistogramAccumulation merge(
      ExponentialHistogramAccumulation previous, ExponentialHistogramAccumulation current) {

    // Create merged buckets
    ExponentialHistogramBuckets posBuckets =
        merge(previous.getPositiveBuckets(), current.getPositiveBuckets());
    ExponentialHistogramBuckets negBuckets =
        merge(previous.getNegativeBuckets(), current.getNegativeBuckets());

    // resolve possible scale difference due to merge
    int commonScale = Math.min(posBuckets.getScale(), negBuckets.getScale());
    posBuckets = downscale(posBuckets, commonScale);
    negBuckets = downscale(negBuckets, commonScale);
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
        commonScale,
        previous.getSum() + current.getSum(),
        previous.hasMinMax() || current.hasMinMax(),
        min,
        max,
        posBuckets,
        negBuckets,
        previous.getZeroCount() + current.getZeroCount(),
        current.getExemplars());
  }

  /**
   * Merge the exponential histogram buckets. If {@code a} is empty, return {@code b}. If {@code b}
   * is empty, return {@code a}. Else merge {@code b} into {@code a}.
   *
   * <p>Assumes {@code a} and {@code b} are either {@link DoubleExponentialHistogramBuckets} or
   * {@link EmptyExponentialHistogramBuckets}.
   */
  private static ExponentialHistogramBuckets merge(
      ExponentialHistogramBuckets a, ExponentialHistogramBuckets b) {
    if (a instanceof EmptyExponentialHistogramBuckets || a.getTotalCount() == 0) {
      return b;
    }
    if (b instanceof EmptyExponentialHistogramBuckets || b.getTotalCount() == 0) {
      return a;
    }
    if ((a instanceof DoubleExponentialHistogramBuckets)
        && (b instanceof DoubleExponentialHistogramBuckets)) {
      DoubleExponentialHistogramBuckets a1 = (DoubleExponentialHistogramBuckets) a;
      DoubleExponentialHistogramBuckets b2 = (DoubleExponentialHistogramBuckets) b;
      a1.mergeInto(b2);
      return a1;
    }
    throw new IllegalStateException(
        "Unable to merge ExponentialHistogramBuckets. Unrecognized implementation.");
  }

  /**
   * Downscale the {@code buckets} to the {@code targetScale}.
   *
   * <p>Assumes {@code a} and {@code b} are either {@link DoubleExponentialHistogramBuckets} or
   * {@link EmptyExponentialHistogramBuckets}.
   */
  private static ExponentialHistogramBuckets downscale(
      ExponentialHistogramBuckets buckets, int targetScale) {
    if (buckets.getScale() == targetScale) {
      return buckets;
    }
    if (buckets instanceof EmptyExponentialHistogramBuckets) {
      return EmptyExponentialHistogramBuckets.get(targetScale);
    }
    if (buckets instanceof DoubleExponentialHistogramBuckets) {
      DoubleExponentialHistogramBuckets buckets1 = (DoubleExponentialHistogramBuckets) buckets;
      buckets1.downscale(buckets1.getScale() - targetScale);
      return buckets1;
    }
    throw new IllegalStateException(
        "Unable to merge ExponentialHistogramBuckets. Unrecognized implementation");
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
    protected synchronized ExponentialHistogramAccumulation doAccumulateThenReset(
        List<DoubleExemplarData> exemplars) {
      ExponentialHistogramBuckets positiveBuckets;
      ExponentialHistogramBuckets negativeBuckets;
      if (this.positiveBuckets != null) {
        positiveBuckets = this.positiveBuckets.copy();
        this.positiveBuckets.clear();
      } else {
        positiveBuckets = EmptyExponentialHistogramBuckets.get(scale);
      }
      if (this.negativeBuckets != null) {
        negativeBuckets = this.negativeBuckets.copy();
        this.negativeBuckets.clear();
      } else {
        negativeBuckets = EmptyExponentialHistogramBuckets.get(scale);
      }
      ExponentialHistogramAccumulation acc =
          ExponentialHistogramAccumulation.create(
              scale,
              sum,
              this.count > 0,
              this.count > 0 ? this.min : -1,
              this.count > 0 ? this.max : -1,
              positiveBuckets,
              negativeBuckets,
              zeroCount,
              exemplars);
      this.sum = 0;
      this.zeroCount = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
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
