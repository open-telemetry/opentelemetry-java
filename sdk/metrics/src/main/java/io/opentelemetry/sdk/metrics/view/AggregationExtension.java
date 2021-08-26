/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;

/** Unspecified extensions for configuring aggregation on Views. */
public class AggregationExtension {
  private AggregationExtension() {}

  /** Records a count of all measurements seen, reported as a monotonic Sum. */
  public static AggregationConfig count(AggregationTemporality temporality) {
    return AggregationConfig.make("count", i -> AggregatorFactory.count(temporality));
  }

  /**
   * Aggregates measurements, preserving a count of all measurements seen, a sum of all measurements
   * and the maximum and minimum values.
   *
   * <p>Reports as a Summary metric.
   */
  public static AggregationConfig minMaxSumCount() {
    return AggregationConfig.make("minMaxSumCount", i -> AggregatorFactory.minMaxSumCount());
  }
}
