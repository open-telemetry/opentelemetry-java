/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;

/** Explicit bucket histogram aggregation configuration. */
class ExplicitBucketHistogramAggregation extends Aggregation {

  static final Aggregation DEFAULT =
      new ExplicitBucketHistogramAggregation(
          AggregationTemporality.CUMULATIVE,
          ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);

  private final AggregationTemporality temporality;
  private final List<Double> bucketBoundaries;

  ExplicitBucketHistogramAggregation(
      AggregationTemporality temporality, List<Double> bucketBoundaries) {
    this.temporality = temporality;
    this.bucketBoundaries = bucketBoundaries;
  }

  /** Returns the configured bucket boundaries for the histogram aggregation. */
  public List<Double> getBucketBoundaries() {
    return bucketBoundaries;
  }

  /** Returns the configured temporality for the histogram aggregation. */
  public AggregationTemporality getTemporality() {
    return temporality;
  }

  @Override
  public <T> Aggregator<T> createAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      ExemplarFilter exemplarFilter) {

    return AggregatorFactory.histogram(bucketBoundaries, temporality)
        .create(
            resource,
            instrumentationLibraryInfo,
            instrumentDescriptor,
            metricDescriptor,
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
