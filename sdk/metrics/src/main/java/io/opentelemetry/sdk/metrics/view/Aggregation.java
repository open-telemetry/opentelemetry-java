/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import java.util.List;

/**
 * Configures how measurements are combined into metrics for {@link View}s.
 *
 * <p>Aggregation provides a set of built-in aggregations via static methods.
 */
public abstract class Aggregation {
  Aggregation() {}

  /** The drop Aggregation will ignore/drop all Instrument Measurements. */
  public static Aggregation drop() {
    return DropAggregation.INSTANCE;
  }

  /** The default aggregation for an instrument will be chosen. */
  public static Aggregation defaultAggregation() {
    return DefaultAggregation.INSTANCE;
  }

  /** Instrument measurements will be combined into a metric Sum. */
  public static Aggregation sum() {
    return SumAggregation.DEFAULT;
  }

  /** Remembers the last seen measurement and reports as a Gauge. */
  public static Aggregation lastValue() {
    return LastValueAggregation.INSTANCE;
  }

  /**
   * Aggregates measurements into an explicit bucket histogram using the default bucket boundaries.
   */
  public static Aggregation explicitBucketHistogram() {
    return ExplicitBucketHistogramAggregation.DEFAULT;
  }

  /**
   * Aggregates measurements into an explicit bucket histogram.
   *
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  public static Aggregation explicitBucketHistogram(List<Double> bucketBoundaries) {
    return new ExplicitBucketHistogramAggregation(bucketBoundaries);
  }

  /** Aggregates measurements using the best available Histogram. */
  public static Aggregation histogram() {
    return explicitBucketHistogram();
  }
}
