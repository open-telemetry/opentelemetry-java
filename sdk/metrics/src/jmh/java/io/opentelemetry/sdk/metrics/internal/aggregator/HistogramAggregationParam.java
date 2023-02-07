/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import java.util.Collections;

/** The types of histogram aggregation to benchmark. */
@SuppressWarnings("ImmutableEnumChecker")
public enum HistogramAggregationParam {
  EXPLICIT_DEFAULT_BUCKET(
      new DoubleExplicitBucketHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(
              ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES),
          ExemplarReservoir::doubleNoSamples)),
  EXPLICIT_SINGLE_BUCKET(
      new DoubleExplicitBucketHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(Collections.emptyList()),
          ExemplarReservoir::doubleNoSamples)),
  EXPONENTIAL_SMALL_CIRCULAR_BUFFER(
      new DoubleBase2ExponentialHistogramAggregator(ExemplarReservoir::doubleNoSamples, 20, 0)),
  EXPONENTIAL_CIRCULAR_BUFFER(
      new DoubleBase2ExponentialHistogramAggregator(ExemplarReservoir::doubleNoSamples, 160, 0));

  private final Aggregator<?, ?> aggregator;

  HistogramAggregationParam(Aggregator<?, ?> aggregator) {
    this.aggregator = aggregator;
  }

  public Aggregator<?, ?> getAggregator() {
    return this.aggregator;
  }
}
