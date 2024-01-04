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
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleExplicitBucketHistogramAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import java.util.List;

/**
 * Explicit bucket histogram aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExplicitBucketHistogramAggregation implements AggregationExtension {

  private static final Aggregation DEFAULT =
      new ExplicitBucketHistogramAggregation(
          ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES,
          ExemplarReservoirFactory.histogramBucket(
              Clock.getDefault(),
              ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES));

  public static Aggregation getDefault() {
    return DEFAULT;
  }

  public static Aggregation create(List<Double> bucketBoundaries) {
    return new ExplicitBucketHistogramAggregation(
        bucketBoundaries,
        ExemplarReservoirFactory.histogramBucket(Clock.getDefault(), bucketBoundaries));
  }

  private final List<Double> bucketBoundaries;
  private final double[] bucketBoundaryArray;
  private final ExemplarReservoirFactory reservoirFactory;

  private ExplicitBucketHistogramAggregation(
      List<Double> bucketBoundaries, ExemplarReservoirFactory reservoirFactory) {
    this.bucketBoundaries = bucketBoundaries;
    // We need to fail here if our bucket boundaries are ill-configured.
    this.bucketBoundaryArray = ExplicitBucketHistogramUtils.createBoundaryArray(bucketBoundaries);
    this.reservoirFactory = reservoirFactory;
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
                    exemplarFilter, reservoirFactory.createDoubleExemplarReservoir()));
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
    return "ExplicitBucketHistogramAggregation(" + bucketBoundaries.toString() + ")";
  }

  @Override
  public AggregationExtension setExemplarReservoirFactory(
      ExemplarReservoirFactory reservoirFactory) {
    return new ExplicitBucketHistogramAggregation(this.bucketBoundaries, reservoirFactory);
  }
}
