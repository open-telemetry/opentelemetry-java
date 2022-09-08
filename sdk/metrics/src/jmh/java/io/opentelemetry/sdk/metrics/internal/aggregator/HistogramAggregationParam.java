/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;
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
      new DoubleExponentialHistogramAggregator(
          ExemplarReservoir::doubleNoSamples,
          ExponentialBucketStrategy.newStrategy(
              20, ExponentialCounterFactory.circularBufferCounter()))),
  EXPONENTIAL_CIRCULAR_BUFFER(
      new DoubleExponentialHistogramAggregator(
          ExemplarReservoir::doubleNoSamples,
          ExponentialBucketStrategy.newStrategy(
              160, ExponentialCounterFactory.circularBufferCounter()))),
  EXPONENTIAL_MAP_COUNTER(
      new DoubleExponentialHistogramAggregator(
          ExemplarReservoir::doubleNoSamples,
          ExponentialBucketStrategy.newStrategy(160, ExponentialCounterFactory.mapCounter())));

  private final Aggregator<?, ?> aggregator;

  private HistogramAggregationParam(Aggregator<?, ?> aggregator) {
    this.aggregator = aggregator;
  }

  public Aggregator<?, ?> getAggregator() {
    return this.aggregator;
  }
}
