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

  private static final String AGGREGATION_DEFAULT = "default";
  private static final String AGGREGATION_SUM = "sum";
  private static final String AGGREGATION_LAST_VALUE = "last_value";
  private static final String AGGREGATION_DROP = "drop";
  private static final String AGGREGATION_EXPLICIT_BUCKET_HISTOGRAM = "explicit_bucket_histogram";

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

  /**
   * Return the aggregation for the human-readable {@code name}.
   *
   * <p>The inverse of {@link #aggregationName(Aggregation)}.
   *
   * @throws IllegalArgumentException if the name is not recognized
   */
  public static Aggregation forName(String name) {
    switch (name) {
      case AGGREGATION_DEFAULT:
        return defaultAggregation();
      case AGGREGATION_SUM:
        return sum();
      case AGGREGATION_LAST_VALUE:
        return lastValue();
      case AGGREGATION_DROP:
        return drop();
      case AGGREGATION_EXPLICIT_BUCKET_HISTOGRAM:
        return explicitBucketHistogram();
      default:
        throw new IllegalArgumentException("Unrecognized aggregation name " + name);
    }
  }

  /**
   * Return the human-readable name of the {@code aggregation}.
   *
   * <p>The inverse of {@link #forName(String)}.
   */
  public static String aggregationName(Aggregation aggregation) {
    if (aggregation instanceof DefaultAggregation) {
      return AGGREGATION_DEFAULT;
    }
    if (aggregation instanceof SumAggregation) {
      return AGGREGATION_SUM;
    }
    if (aggregation instanceof LastValueAggregation) {
      return AGGREGATION_LAST_VALUE;
    }
    if (aggregation instanceof DropAggregation) {
      return AGGREGATION_DROP;
    }
    if (aggregation instanceof ExplicitBucketHistogramAggregation) {
      return AGGREGATION_EXPLICIT_BUCKET_HISTOGRAM;
    }
    // This should not happen
    throw new IllegalStateException("Unrecognized aggregation " + aggregation.getClass().getName());
  }
}
