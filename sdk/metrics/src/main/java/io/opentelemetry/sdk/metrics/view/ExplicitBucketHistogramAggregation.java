/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import java.util.List;

/** Explciit bucket histogram aggregation configuration. */
public class ExplicitBucketHistogramAggregation extends Aggregation {
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
  public AggregatorFactory getFactory(InstrumentDescriptor instrument) {
    return AggregatorFactory.histogram(bucketBoundaries, temporality);
  }

  @Override
  public Aggregation resolve(InstrumentDescriptor instrument) {
    return this;
  }

  @Override
  public String toString() {
    return "explicitBucketHistogram(" + temporality + ")";
  }
}
