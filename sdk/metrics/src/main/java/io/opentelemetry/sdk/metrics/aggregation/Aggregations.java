/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

public final class Aggregations {
  private Aggregations() {}

  /**
   * Returns an {@code Aggregation} that calculates sum of recorded measurements.
   *
   * @return an {@code Aggregation} that calculates sum of recorded measurements.
   */
  public static Aggregation sum() {
    return SumAggregation.INSTANCE;
  }

  /**
   * Returns an {@code Aggregation} that calculates count of recorded measurements (the number of
   * recorded measurements).
   *
   * @return an {@code Aggregation} that calculates count of recorded measurements (the number of
   *     recorded * measurements).
   */
  public static Aggregation count() {
    return CountAggregation.INSTANCE;
  }

  /**
   * Returns an {@code Aggregation} that calculates the last value of all recorded measurements.
   *
   * @return an {@code Aggregation} that calculates the last value of all recorded measurements.
   */
  public static Aggregation lastValue() {
    return LastValueAggregation.INSTANCE;
  }

  /**
   * Returns an {@code Aggregation} that calculates a simple summary of all recorded measurements.
   * The summary consists of the count of measurements, the sum of all measurements, the maximum
   * value recorded and the minimum value recorded.
   *
   * @return an {@code Aggregation} that calculates a simple summary of all recorded measurements.
   */
  public static Aggregation minMaxSumCount() {
    return MinMaxSumCountAggregation.INSTANCE;
  }
}
