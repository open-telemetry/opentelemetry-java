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
          ExemplarReservoir::noSamples)),
  EXPLICIT_SINGLE_BUCKET(
      new DoubleExplicitBucketHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(Collections.emptyList()),
          ExemplarReservoir::noSamples)),
  EXPONENTIAL_SMALL_CIRCULAR_BUFFER(
      new DoubleExponentialHistogramAggregator(
          ExemplarReservoir::noSamples,
          ExponentialBucketStrategy.newStrategy(
              20, 20, ExponentialCounterFactory.circularBufferCounter()))),
  EXPONENTIAL_CIRCULAR_BUFFER(
      new DoubleExponentialHistogramAggregator(
          ExemplarReservoir::noSamples,
          ExponentialBucketStrategy.newStrategy(
              20, 320, ExponentialCounterFactory.circularBufferCounter()))),
  EXPONENTIAL_MAP_COUNTER(
      new DoubleExponentialHistogramAggregator(
          ExemplarReservoir::noSamples,
          ExponentialBucketStrategy.newStrategy(20, 320, ExponentialCounterFactory.mapCounter())));

  private final Aggregator<?> aggregator;

  private HistogramAggregationParam(Aggregator<?> aggregator) {
    this.aggregator = aggregator;
  }

  public Aggregator<?> getAggregator() {
    return this.aggregator;
  }
}
