/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleHistogramAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.List;
import javax.annotation.Nullable;

/** Explicit bucket histogram aggregation configuration. */
class ExplicitBucketHistogramAggregation extends Aggregation {

  static final Aggregation DEFAULT =
      new ExplicitBucketHistogramAggregation(
          null, ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);

  @Nullable private final AggregationTemporality temporality;
  private final List<Double> bucketBoundaries;
  private final double[] bucketBoundaryArray;

  ExplicitBucketHistogramAggregation(
      @Nullable AggregationTemporality temporality, List<Double> bucketBoundaries) {
    this.temporality = temporality;
    this.bucketBoundaries = bucketBoundaries;
    // We need to fail here if our bucket boundaries are ill-configured.
    this.bucketBoundaryArray = ExplicitBucketHistogramUtils.createBoundaryArray(bucketBoundaries);
  }

  /** Returns the configured bucket boundaries for the histogram aggregation. */
  public List<Double> getBucketBoundaries() {
    return bucketBoundaries;
  }

  @Override
  @Nullable
  public AggregationTemporality getConfiguredTemporality() {
    return temporality;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return (Aggregator<T>)
        new DoubleHistogramAggregator(
            bucketBoundaryArray,
            () ->
                ExemplarReservoir.filtered(
                    exemplarFilter,
                    ExemplarReservoir.histogramBucketReservoir(
                        Clock.getDefault(), bucketBoundaries)));
  }

  @Override
  public String toString() {
    return "ExplicitBucketHistogramAggregation(" + temporality + ")";
  }
}
