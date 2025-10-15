/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.EmptyExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Aggregator that generates base2 exponential histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleBase2ExponentialHistogramAggregator
    implements Aggregator<ExponentialHistogramPointData> {

  private final ExemplarReservoirFactory reservoirFactory;
  private final int maxBuckets;
  private final int maxScale;
  private final MemoryMode memoryMode;

  /**
   * Constructs an exponential histogram aggregator.
   *
   * @param reservoirFactory Supplier of exemplar reservoirs per-stream.
   */
  public DoubleBase2ExponentialHistogramAggregator(
      ExemplarReservoirFactory reservoirFactory,
      int maxBuckets,
      int maxScale,
      MemoryMode memoryMode) {
    this.reservoirFactory = reservoirFactory;
    this.maxBuckets = maxBuckets;
    this.maxScale = maxScale;
    this.memoryMode = memoryMode;
  }

  @Override
  public AggregatorHandle<ExponentialHistogramPointData> createHandle() {
    return new Handle(reservoirFactory, maxBuckets, maxScale, memoryMode);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Collection<ExponentialHistogramPointData> points,
      AggregationTemporality temporality) {
    return ImmutableMetricData.createExponentialHistogram(
        resource,
        instrumentationScopeInfo,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getSourceInstrument().getUnit(),
        ImmutableExponentialHistogramData.create(temporality, points));
  }

  static final class Handle extends AggregatorHandle<ExponentialHistogramPointData> {
    private final int maxBuckets;
    private final int maxScale;
    @Nullable private DoubleBase2ExponentialHistogramBuckets positiveBuckets;
    @Nullable private DoubleBase2ExponentialHistogramBuckets negativeBuckets;
    private long zeroCount;
    private double sum;
    private double min;
    private double max;
    private long count;
    private int currentScale;
    private final MemoryMode memoryMode;

    // Used only when MemoryMode = REUSABLE_DATA
    @Nullable private final MutableExponentialHistogramPointData reusablePoint;

    Handle(
        ExemplarReservoirFactory reservoirFactory,
        int maxBuckets,
        int maxScale,
        MemoryMode memoryMode) {
      super(reservoirFactory);
      this.maxBuckets = maxBuckets;
      this.maxScale = maxScale;
      this.sum = 0;
      this.zeroCount = 0;
      this.min = Double.MAX_VALUE;
      this.max = -1;
      this.count = 0;
      this.currentScale = maxScale;
      this.reusablePoint =
          (memoryMode == MemoryMode.REUSABLE_DATA)
              ? new MutableExponentialHistogramPointData()
              : null;
      this.memoryMode = memoryMode;
    }

    @Override
    protected synchronized ExponentialHistogramPointData doAggregateThenMaybeResetDoubles(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {

      ExponentialHistogramPointData point;
      if (reusablePoint == null) {
        point =
            ImmutableExponentialHistogramPointData.create(
                currentScale,
                sum,
                zeroCount,
                this.count > 0,
                this.min,
                this.count > 0,
                this.max,
                resolveBuckets(
                    this.positiveBuckets, currentScale, reset, /* reusableBuckets= */ null),
                resolveBuckets(
                    this.negativeBuckets, currentScale, reset, /* reusableBuckets= */ null),
                startEpochNanos,
                epochNanos,
                attributes,
                exemplars);
      } else /* REUSABLE_DATA */ {
        point =
            reusablePoint.set(
                currentScale,
                sum,
                zeroCount,
                this.count > 0,
                this.min,
                this.count > 0,
                this.max,
                resolveBuckets(
                    this.positiveBuckets, currentScale, reset, reusablePoint.getPositiveBuckets()),
                resolveBuckets(
                    this.negativeBuckets, currentScale, reset, reusablePoint.getNegativeBuckets()),
                startEpochNanos,
                epochNanos,
                attributes,
                exemplars);
      }

      if (reset) {
        this.sum = 0;
        this.zeroCount = 0;
        this.min = Double.MAX_VALUE;
        this.max = -1;
        this.count = 0;
        this.currentScale = maxScale;
      }
      return point;
    }

    private ExponentialHistogramBuckets resolveBuckets(
        @Nullable DoubleBase2ExponentialHistogramBuckets buckets,
        int scale,
        boolean reset,
        @Nullable ExponentialHistogramBuckets reusableBuckets) {
      if (buckets == null) {
        return EmptyExponentialHistogramBuckets.get(scale);
      }

      ExponentialHistogramBuckets copy;
      if (reusableBuckets == null) {
        copy = buckets.copy();
      } else {
        MutableExponentialHistogramBuckets mutableExponentialHistogramBuckets;
        if (reusableBuckets instanceof MutableExponentialHistogramBuckets) {
          mutableExponentialHistogramBuckets = (MutableExponentialHistogramBuckets) reusableBuckets;
        } else /* EmptyExponentialHistogramBuckets */ {
          mutableExponentialHistogramBuckets = new MutableExponentialHistogramBuckets();
        }

        DynamicPrimitiveLongList reusableBucketCountsList =
            mutableExponentialHistogramBuckets.getReusableBucketCountsList();
        buckets.getBucketCountsIntoReusableList(reusableBucketCountsList);

        mutableExponentialHistogramBuckets.set(
            buckets.getScale(),
            buckets.getOffset(),
            buckets.getTotalCount(),
            reusableBucketCountsList);

        copy = mutableExponentialHistogramBuckets;
      }

      if (reset) {
        buckets.clear(maxScale);
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
      DoubleBase2ExponentialHistogramBuckets buckets;
      if (c == 0) {
        zeroCount++;
        return;
      } else if (c > 0) {
        // Initialize positive buckets at current scale, if needed
        if (positiveBuckets == null) {
          positiveBuckets =
              new DoubleBase2ExponentialHistogramBuckets(currentScale, maxBuckets, memoryMode);
        }
        buckets = positiveBuckets;
      } else {
        // Initialize negative buckets at current scale, if needed
        if (negativeBuckets == null) {
          negativeBuckets =
              new DoubleBase2ExponentialHistogramBuckets(currentScale, maxBuckets, memoryMode);
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
    protected boolean isDoubleType() {
      return true;
    }

    @Override
    public void recordLong(long value, Attributes attributes, Context context) {
      // Since there is no LongExplicitBucketHistogramAggregator and we need to support measurements
      // from LongHistogram, we redirect calls from #recordLong to #recordDouble. Without this, the
      // base AggregatorHandle implementation of #recordLong throws.
      super.recordDouble((double) value, attributes, context);
    }

    void downScale(int by) {
      if (positiveBuckets != null) {
        positiveBuckets.downscale(by);
        currentScale = positiveBuckets.getScale();
      }
      if (negativeBuckets != null) {
        negativeBuckets.downscale(by);
        currentScale = negativeBuckets.getScale();
      }
    }
  }
}
