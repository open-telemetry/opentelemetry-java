/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleExplicitBucketHistogramAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import java.util.List;
import java.util.Objects;

/**
 * Explicit bucket histogram aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExplicitBucketHistogramAggregation implements Aggregation, AggregatorFactory {

  private static final Aggregation DEFAULT =
      new ExplicitBucketHistogramAggregation(
          ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);

  public static Aggregation getDefault() {
    return DEFAULT;
  }

  public static Aggregation create(List<Double> bucketBoundaries) {
    return new ExplicitBucketHistogramAggregation(bucketBoundaries);
  }

  private final List<Double> bucketBoundaries;
  private final double[] bucketBoundaryArray;

  private ExplicitBucketHistogramAggregation(List<Double> bucketBoundaries) {
    this.bucketBoundaries = bucketBoundaries;
    // We need to fail here if our bucket boundaries are ill-configured.
    this.bucketBoundaryArray = ExplicitBucketHistogramUtils.createBoundaryArray(bucketBoundaries);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends PointData, U extends ExemplarData> Aggregator<T, U> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return (Aggregator<T, U>)
        new DoubleExplicitBucketHistogramAggregator(
            bucketBoundaryArray,
            () ->
                ExemplarReservoir.filtered(
                    exemplarFilter,
                    ExemplarReservoir.histogramBucketReservoir(
                        Clock.getDefault(), bucketBoundaries)));
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ExplicitBucketHistogramAggregation that = (ExplicitBucketHistogramAggregation) o;

    return Objects.equals(bucketBoundaries, that.bucketBoundaries);
  }

  @Override
  public int hashCode() {
    return bucketBoundaries != null ? bucketBoundaries.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "ExplicitBucketHistogramAggregation(" + bucketBoundaries.toString() + ")";
  }
}
