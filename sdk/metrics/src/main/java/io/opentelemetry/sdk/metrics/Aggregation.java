/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.internal.view.DefaultAggregation;
import io.opentelemetry.sdk.metrics.internal.view.DropAggregation;
import io.opentelemetry.sdk.metrics.internal.view.ExplicitBucketHistogramAggregation;
import io.opentelemetry.sdk.metrics.internal.view.LastValueAggregation;
import io.opentelemetry.sdk.metrics.internal.view.SumAggregation;
import java.util.List;

/**
 * Configures how measurements are combined into metrics.
 *
 * <p>Aggregation provides a set of built-in aggregations via static methods.
 */
// TODO(anuraaga): Have methods when custom aggregations are supported.
@SuppressWarnings("InterfaceWithOnlyStatics")
public interface Aggregation {

  /** The drop Aggregation will ignore/drop all Instrument Measurements. */
  static Aggregation drop() {
    return DropAggregation.getInstance();
  }

  /** The default aggregation for an instrument will be chosen. */
  static Aggregation defaultAggregation() {
    return DefaultAggregation.getInstance();
  }

  /** Instrument measurements will be combined into a metric Sum. */
  static Aggregation sum() {
    return SumAggregation.getInstance();
  }

  /** Remembers the last seen measurement and reports as a Gauge. */
  static Aggregation lastValue() {
    return LastValueAggregation.getInstance();
  }

  /**
   * Aggregates measurements into an explicit bucket histogram using the default bucket boundaries.
   */
  static Aggregation explicitBucketHistogram() {
    return ExplicitBucketHistogramAggregation.getDefault();
  }

  /**
   * Aggregates measurements into an explicit bucket histogram.
   *
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  static Aggregation explicitBucketHistogram(List<Double> bucketBoundaries) {
    return ExplicitBucketHistogramAggregation.create(bucketBoundaries);
  }
}
