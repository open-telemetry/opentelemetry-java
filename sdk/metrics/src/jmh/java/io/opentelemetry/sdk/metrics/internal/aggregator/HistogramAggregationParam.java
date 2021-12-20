/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.Collections;

/** The types of histogram aggregation to benchmark. */
@SuppressWarnings("ImmutableEnumChecker")
public enum HistogramAggregationParam {
  EXPLICIT_DEFAULT_BUCKET(
      new DoubleHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(
              ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES),
          ExemplarReservoir::noSamples)),
  EXPLICIT_SINGLE_BUCKET(
      new DoubleHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(Collections.emptyList()),
          ExemplarReservoir::noSamples)),
  EXPONENTIAL(new DoubleExponentialHistogramAggregator(ExemplarReservoir::noSamples));

  private final Aggregator<?> aggregator;

  private HistogramAggregationParam(Aggregator<?> aggregator) {
    this.aggregator = aggregator;
  }

  public Aggregator<?> getAggregator() {
    return this.aggregator;
  }
}
