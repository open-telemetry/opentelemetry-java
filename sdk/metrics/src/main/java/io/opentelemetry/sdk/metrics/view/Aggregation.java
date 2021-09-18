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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures how measurements are combined into metrics for {@link View}s.
 *
 * <p>Aggregation provides a set of built-in aggregations via static methods.
 */
public abstract class Aggregation {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(Aggregation.class.getName()));

  private static final Aggregation NONE = Aggregation.make("none", unused -> null);
  private static final Aggregation DEFAULT =
      Aggregation.make(
          "default",
          i -> {
            switch (i.getType()) {
              case COUNTER:
              case UP_DOWN_COUNTER:
              case OBSERVABLE_SUM:
              case OBSERVABLE_UP_DOWN_SUM:
                return AggregatorFactory.sum(AggregationTemporality.CUMULATIVE);
              case HISTOGRAM:
                return AggregatorFactory.histogram(
                    ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES,
                    AggregationTemporality.CUMULATIVE);
              case OBSERVABLE_GAUGE:
                return AggregatorFactory.lastValue();
            }
            logger.log(Level.WARNING, "Unable to find default aggregation for instrument: " + i);
            return null;
          });
  private static final Aggregation SUM = sum(AggregationTemporality.CUMULATIVE);
  private static final Aggregation LAST_VALUE =
      Aggregation.make("lastValue", unused -> AggregatorFactory.lastValue());
  private static final Aggregation EXPLICIT_BUCKET_HISTOGRAM =
      explictBucketHistogram(
          AggregationTemporality.CUMULATIVE,
          ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);

  private Aggregation() {}

  /**
   * Returns the appropriate aggregator factory for a given instrument.
   *
   * @return The AggregatorFactory or {@code null} if none.
   */
  public abstract AggregatorFactory config(InstrumentDescriptor instrument);

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
    return Aggregation.make("sum", unused -> AggregatorFactory.sum(temporality));
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
  public static Aggregation explictBucketHistogram(AggregationTemporality temporality) {
    return explictBucketHistogram(
        temporality, ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);
  }

  /**
   * Aggregates measurements into an explicit bucket histogram.
   *
   * @param temporality Whether to report DELTA or CUMULATIVE metrics.
   * @param bucketBoundaries A list of (inclusive) upper bounds for the histogram. Should be in
   *     order from lowest to highest.
   */
  public static Aggregation explictBucketHistogram(
      AggregationTemporality temporality, List<Double> bucketBoundaries) {
    return Aggregation.make(
        "explicitBucketHistogram",
        unused -> AggregatorFactory.histogram(bucketBoundaries, temporality));
  }

  /** Aggregates measurements using the best available Histogram. */
  public static final Aggregation histogram() {
    return EXPLICIT_BUCKET_HISTOGRAM;
  }

  static Aggregation make(String name, Function<InstrumentDescriptor, AggregatorFactory> factory) {
    return new Aggregation() {

      @Override
      public AggregatorFactory config(InstrumentDescriptor instrument) {
        return factory.apply(instrument);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }
}
