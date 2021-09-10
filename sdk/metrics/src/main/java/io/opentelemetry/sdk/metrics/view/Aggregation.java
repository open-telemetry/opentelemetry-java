/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Configures how measurements are combined into metrics for {@link View}s.
 *
 * <p>Aggregation provides a set of built-in aggregations via static methods.
 */
public abstract class Aggregation {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(Aggregation.class.getName()));

  private static final Aggregation NONE = new NoAggregation();
  private static final Aggregation DEFAULT =
      new Aggregation() {
        @Override
        public AggregatorFactory getFactory(InstrumentDescriptor instrument) {
          return resolve(instrument).getFactory(instrument);
        }

        @Override
        public Aggregation resolve(InstrumentDescriptor instrument) {
          switch (instrument.getType()) {
            case COUNTER:
            case UP_DOWN_COUNTER:
            case OBSERVABLE_SUM:
            case OBSERVABLE_UP_DOWN_SUM:
              return SUM;
            case HISTOGRAM:
              return EXPLICIT_BUCKET_HISTOGRAM;
            case OBSERVABLE_GAUGE:
              return LAST_VALUE;
          }
          logger.log(
              Level.WARNING, "Unable to find default aggregation for instrument: " + instrument);
          return NONE;
        }

        @Override
        public String toString() {
          return "default";
        }
      };
  private static final Aggregation SUM = sum(AggregationTemporality.CUMULATIVE);
  private static final Aggregation LAST_VALUE = new LastValueAggregation();
  private static final Aggregation EXPLICIT_BUCKET_HISTOGRAM =
      explicitBucketHistogram(
          AggregationTemporality.CUMULATIVE,
          ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);

  Aggregation() {}

  /**
   * Returns the appropriate aggregator factory for a given instrument.
   *
   * @return The AggregatorFactory or {@code null} if none.
   */
  @Nullable
  public abstract AggregatorFactory getFactory(InstrumentDescriptor instrument);

  /** Resolve from a generic aggregation to a specific instance. */
  public abstract Aggregation resolve(InstrumentDescriptor instrument);

  /** The None Aggregation will ignore/drop all Instrument Measurements. */
  public static Aggregation none() {
    return NONE;
  }

  /** The default aggregation for an instrument will be chosen. */
  public static Aggregation defaultAggregation() {
    return DEFAULT;
  }

  /** Instrument measurements will be combined into a metric Sum. */
  public static Aggregation sum(AggregationTemporality temporality) {
    return new SumAggregation(temporality);
  }

  /** Instrument measurements will be combined into a metric Sum. */
  public static Aggregation sum() {
    return SUM;
  }

  /** Remembers the last seen measurement and reports as a Gauge. */
  public static Aggregation lastValue() {
    return LAST_VALUE;
  }

  /**
   * Aggregates measurements into an explicit bucket histogram using the default bucket boundaries.
   */
  public static Aggregation explictBucketHistogram() {
    return EXPLICIT_BUCKET_HISTOGRAM;
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
   * @param temporality Whether to report DELTA or CUMULATIVE metrics.
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  public static Aggregation explicitBucketHistogram(
      AggregationTemporality temporality, List<Double> bucketBoundaries) {
    return new ExplicitBucketHistogramAggregation(temporality, bucketBoundaries);
  }

  /** Aggregates measurements using the best available Histogram. */
  public static final Aggregation histogram() {
    return EXPLICIT_BUCKET_HISTOGRAM;
  }
}
