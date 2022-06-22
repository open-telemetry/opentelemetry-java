/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.view.DefaultAggregation;
import io.opentelemetry.sdk.metrics.internal.view.DropAggregation;
import io.opentelemetry.sdk.metrics.internal.view.ExplicitBucketHistogramAggregation;
import io.opentelemetry.sdk.metrics.internal.view.LastValueAggregation;
import io.opentelemetry.sdk.metrics.internal.view.SumAggregation;
import java.util.List;

/**
 * Configures how instrument measurements are combined into metrics.
 *
 * <p>Aggregation provides a set of built-in aggregations via static methods.
 *
 * @since 1.14.0
 */
// TODO(anuraaga): Have methods when custom aggregations are supported.
@SuppressWarnings("InterfaceWithOnlyStatics")
public interface Aggregation {

  /** Drops all measurements and don't export any metric. */
  static Aggregation drop() {
    return DropAggregation.getInstance();
  }

  /** Choose the default aggregation for the {@link InstrumentType}. */
  static Aggregation defaultAggregation() {
    return DefaultAggregation.getInstance();
  }

  /**
   * Aggregates measurements into a {@link MetricDataType#DOUBLE_SUM} or {@link
   * MetricDataType#LONG_SUM}.
   */
  static Aggregation sum() {
    return SumAggregation.getInstance();
  }

  /**
   * Records the last seen measurement as a {@link MetricDataType#DOUBLE_GAUGE} or {@link
   * MetricDataType#LONG_GAUGE}.
   */
  static Aggregation lastValue() {
    return LastValueAggregation.getInstance();
  }

  /**
   * Aggregates measurements into an explicit bucket {@link MetricDataType#HISTOGRAM} using the
   * default bucket boundaries.
   */
  static Aggregation explicitBucketHistogram() {
    return ExplicitBucketHistogramAggregation.getDefault();
  }

  /**
   * Aggregates measurements into an explicit bucket {@link MetricDataType#HISTOGRAM}.
   *
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  static Aggregation explicitBucketHistogram(List<Double> bucketBoundaries) {
    return ExplicitBucketHistogramAggregation.create(bucketBoundaries);
  }
}
