/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Configures how measurements are combined into metrics for {@link View}s.
 *
 * <p>Aggregation provides a set of built-in aggregations via static methods.
 */
public abstract class Aggregation {
  Aggregation() {}

  /**
   * Returns a new {@link Aggregator}.
   *
   * @param instrumentDescriptor the descriptor of the {@code Instrument} that will record
   *     measurements.
   * @param exemplarFilter the filter on which measurements should turn into exemplars
   * @return a new {@link Aggregator}, or {@code null} if no measurements should be recorded.
   */
  @Nullable
  public abstract <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter);

  /**
   * Returns the user-configured {@link AggregationTemporality} for this aggregation.
   *
   * @return the temporality, or {code null} if no temporality was specified.
   */
  @Nullable
  public AggregationTemporality getConfiguredTemporality() {
    return null;
  }

  /** The None Aggregation will ignore/drop all Instrument Measurements. */
  public static Aggregation none() {
    return NoAggregation.INSTANCE;
  }

  /** The default aggregation for an instrument will be chosen. */
  public static Aggregation defaultAggregation() {
    return DefaultAggregation.INSTANCE;
  }

  /** Instrument measurements will be combined into a metric Sum. */
  public static Aggregation sum(AggregationTemporality temporality) {
    return new SumAggregation(temporality);
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
   * Aggregates measurements into an explicit bucket histogram using the default bucket boundaries.
   *
   * @param temporality Whether to report DELTA or CUMULATIVE metrics.
   */
  public static Aggregation explicitBucketHistogram(AggregationTemporality temporality) {
    return explicitBucketHistogram(
        temporality, ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);
  }

  /**
   * Aggregates measurements into an explicit bucket histogram.
   *
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  public static Aggregation explicitBucketHistogram(List<Double> bucketBoundaries) {
    return new ExplicitBucketHistogramAggregation(null, bucketBoundaries);
  }

  /**
   * Aggregates measurements into an explicit bucket histogram.
   *
   * @param temporality Whether to report DELTA or CUMULATIVE metrics.
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  public static Aggregation explicitBucketHistogram(
      AggregationTemporality temporality, List<Double> bucketBoundaries) {
    return new ExplicitBucketHistogramAggregation(temporality, bucketBoundaries);
  }

  /** Aggregates measurements using the best available Histogram. */
  public static Aggregation histogram() {
    return explicitBucketHistogram();
  }
}
