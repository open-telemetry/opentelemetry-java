/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.Aggregation;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class AggregationUtil {

  private static final Map<String, Aggregation> aggregationByName;
  private static final Map<Class<? extends Aggregation>, String> nameByAggregation;

  private static final String AGGREGATION_DEFAULT = "default";
  private static final String AGGREGATION_SUM = "sum";
  private static final String AGGREGATION_LAST_VALUE = "last_value";
  private static final String AGGREGATION_DROP = "drop";
  private static final String AGGREGATION_EXPLICIT_BUCKET_HISTOGRAM = "explicit_bucket_histogram";
  private static final String AGGREGATION_BASE2_EXPONENTIAL_HISTOGRAM =
      "base2_exponential_bucket_histogram";

  static {
    aggregationByName = new HashMap<>();
    aggregationByName.put(AGGREGATION_DEFAULT, Aggregation.defaultAggregation());
    aggregationByName.put(AGGREGATION_SUM, Aggregation.sum());
    aggregationByName.put(AGGREGATION_LAST_VALUE, Aggregation.lastValue());
    aggregationByName.put(AGGREGATION_DROP, Aggregation.drop());
    aggregationByName.put(
        AGGREGATION_EXPLICIT_BUCKET_HISTOGRAM, Aggregation.explicitBucketHistogram());
    aggregationByName.put(
        AGGREGATION_BASE2_EXPONENTIAL_HISTOGRAM, Aggregation.base2ExponentialBucketHistogram());

    nameByAggregation = new HashMap<>();
    nameByAggregation.put(Aggregation.defaultAggregation().getClass(), AGGREGATION_DEFAULT);
    nameByAggregation.put(Aggregation.sum().getClass(), AGGREGATION_SUM);
    nameByAggregation.put(Aggregation.lastValue().getClass(), AGGREGATION_LAST_VALUE);
    nameByAggregation.put(Aggregation.drop().getClass(), AGGREGATION_DROP);
    nameByAggregation.put(
        Aggregation.explicitBucketHistogram().getClass(), AGGREGATION_EXPLICIT_BUCKET_HISTOGRAM);
    nameByAggregation.put(
        Aggregation.base2ExponentialBucketHistogram().getClass(),
        AGGREGATION_BASE2_EXPONENTIAL_HISTOGRAM);
  }

  private AggregationUtil() {}

  /**
   * Return the aggregation for the human-readable {@code name}.
   *
   * <p>The inverse of {@link #aggregationName(Aggregation)}.
   *
   * @throws IllegalArgumentException if the name is not recognized
   */
  public static Aggregation forName(String name) {
    Aggregation aggregation = aggregationByName.get(name.toLowerCase());
    if (aggregation == null) {
      throw new IllegalArgumentException("Unrecognized aggregation name " + name);
    }
    return aggregation;
  }

  /**
   * Return the human-readable name of the {@code aggregation}.
   *
   * <p>The inverse of {@link #forName(String)}.
   *
   * @throws IllegalArgumentException if the aggregation is not recognized
   */
  public static String aggregationName(Aggregation aggregation) {
    String name = nameByAggregation.get(aggregation.getClass());
    if (name == null) {
      throw new IllegalStateException(
          "Unrecognized aggregation " + aggregation.getClass().getName());
    }
    return name;
  }
}
