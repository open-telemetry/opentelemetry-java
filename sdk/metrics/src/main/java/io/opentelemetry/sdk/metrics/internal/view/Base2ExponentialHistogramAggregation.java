/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleBase2ExponentialHistogramAggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;

/**
 * Exponential bucket histogram aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class Base2ExponentialHistogramAggregation implements Aggregation, AggregatorFactory {

  private static final int DEFAULT_MAX_BUCKETS = 160;
  private static final int DEFAULT_MAX_SCALE = 20;

  private static final Aggregation DEFAULT =
      new Base2ExponentialHistogramAggregation(DEFAULT_MAX_BUCKETS, DEFAULT_MAX_SCALE);

  private final int maxBuckets;
  private final int maxScale;

  private Base2ExponentialHistogramAggregation(int maxBuckets, int maxScale) {
    this.maxBuckets = maxBuckets;
    this.maxScale = maxScale;
  }

  public static Aggregation getDefault() {
    return DEFAULT;
  }

  /**
   * Aggregations measurements into an {@link MetricDataType#EXPONENTIAL_HISTOGRAM}.
   *
   * @param maxBuckets the max number of positive buckets and negative buckets (max total buckets is
   *     2 * {@code maxBuckets} + 1 zero bucket).
   * @param maxScale the maximum and initial scale. If measurements can't fit in a particular scale
   *     given the {@code maxBuckets}, the scale is reduced until the measurements can be
   *     accommodated. Setting maxScale may reduce the number of downscales. Additionally, the
   *     performance of computing bucket index is improved when scale is <= 0.
   * @return the aggregation
   */
  public static Aggregation create(int maxBuckets, int maxScale) {
    checkArgument(maxBuckets >= 2, "maxBuckets must be >= 2");
    checkArgument(maxScale <= 20 && maxScale >= -10, "maxScale must be -10 <= x <= 20");
    return new Base2ExponentialHistogramAggregation(maxBuckets, maxScale);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends PointData> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor,
      ExemplarFilterInternal exemplarFilter,
      MemoryMode memoryMode) {
    return (Aggregator<T>)
        new DoubleBase2ExponentialHistogramAggregator(
            ExemplarReservoirFactory.filtered(
                exemplarFilter,
                ExemplarReservoirFactory.fixedSizeReservoir(
                    Clock.getDefault(),
                    Runtime.getRuntime().availableProcessors(),
                    RandomSupplier.platformDefault())),
            maxBuckets,
            maxScale,
            memoryMode);
  }

  @Override
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    switch (instrumentDescriptor.getType()) {
      case COUNTER:
      case HISTOGRAM:
        return true;
      default:
        return false;
    }
  }

  @Override
  public String toString() {
    return "Base2ExponentialHistogramAggregation{maxBuckets="
        + maxBuckets
        + ",maxScale="
        + maxScale
        + "}";
  }
}
