/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.common.export.MemoryMode.IMMUTABLE_DATA;

import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import java.util.Collections;

/** The types of histogram aggregation to benchmark. */
@SuppressWarnings("ImmutableEnumChecker")
public enum HistogramAggregationParam {
  EXPLICIT_DEFAULT_BUCKET(
      new DoubleExplicitBucketHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(
              ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES),
          ExemplarReservoirFactory.noSamples(),
          IMMUTABLE_DATA)),
  EXPLICIT_SINGLE_BUCKET(
      new DoubleExplicitBucketHistogramAggregator(
          ExplicitBucketHistogramUtils.createBoundaryArray(Collections.emptyList()),
          ExemplarReservoirFactory.noSamples(),
          IMMUTABLE_DATA)),
  EXPONENTIAL_SMALL_CIRCULAR_BUFFER(
      new DoubleBase2ExponentialHistogramAggregator(
          ExemplarReservoirFactory.noSamples(), 20, 0, IMMUTABLE_DATA)),
  EXPONENTIAL_CIRCULAR_BUFFER(
      new DoubleBase2ExponentialHistogramAggregator(
          ExemplarReservoirFactory.noSamples(), 160, 0, IMMUTABLE_DATA));

  private final Aggregator<?> aggregator;

  HistogramAggregationParam(Aggregator<?> aggregator) {
    this.aggregator = aggregator;
  }

  public Aggregator<?> getAggregator() {
    return this.aggregator;
  }
}
