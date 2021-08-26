/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Factories for configuring aggregation on Views. */
public class Aggregation {
  private Aggregation() {}

  /** The None Aggregation will ignore/drop all Instrument Measurements. */
  public static AggregatorConfig none() {
    return AggregatorConfig.make("none", i -> null);
  }

  /** The default aggregation for an instrument will be chosen. */
  public static AggregatorConfig defaultAggregation() {
    return AggregatorConfig.make(
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
                  DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES, AggregationTemporality.CUMULATIVE);
            case OBSERVABLE_GAUGE:
              return AggregatorFactory.lastValue();
          }
          // TODO - log unknown descriptor type.
          return null;
        });
  }

  /** Instrument measurements will be combined into a metric Sum. */
  public static AggregatorConfig sum(AggregationTemporality temporality) {
    return AggregatorConfig.make("sum", i -> AggregatorFactory.sum(temporality));
  }

  public static AggregatorConfig sum() {
    return sum(AggregationTemporality.CUMULATIVE);
  }

  // TODO - allow "is_monotonic" as configuration of sums?

  /** Remembers the last seen measurement and reports as a Gauge. */
  public static AggregatorConfig lastValue() {
    return AggregatorConfig.make("lastValue", i -> AggregatorFactory.lastValue());
  }

  /** Aggregates measurements using the best available Histogram. */
  public static AggregatorConfig histogram() {
    return explictBucketHistogram();
  }

  public static AggregatorConfig explictBucketHistogram() {
    return explictBucketHistogram(
        AggregationTemporality.CUMULATIVE, DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);
  }

  public static AggregatorConfig explictBucketHistogram(
      AggregationTemporality temporality, List<Double> bucketBoundaries) {
    return AggregatorConfig.make(
        "explicitBucketHistogram", i -> AggregatorFactory.histogram(bucketBoundaries, temporality));
  }

  static final List<Double> DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d, 5_000d, 7_500d,
              10_000d));
}
