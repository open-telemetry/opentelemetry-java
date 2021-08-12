/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import java.util.Arrays;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class MinMaxSumCountAccumulation {
  /**
   * Creates a new {@link MinMaxSumCountAccumulation} with the given values.
   *
   * @param count the number of measurements.
   * @param sum the sum of the measurements.
   * @param min the min value out of all measurements.
   * @param max the max value out of all measurements.
   * @return a new {@link MinMaxSumCountAccumulation} with the given values.
   */
  static MinMaxSumCountAccumulation create(long count, double sum, double min, double max) {
    return new AutoValue_MinMaxSumCountAccumulation(count, sum, min, max);
  }

  MinMaxSumCountAccumulation() {}

  /**
   * Returns the count (number of measurements) stored by this accumulation.
   *
   * @return the count stored by this accumulation.
   */
  abstract long getCount();

  /**
   * Returns the sum (sum of measurements) stored by this accumulation.
   *
   * @return the sum stored by this accumulation.
   */
  abstract double getSum();

  /**
   * Returns the min (minimum of all measurements) stored by this accumulation.
   *
   * @return the min stored by this accumulation.
   */
  abstract double getMin();

  /**
   * Returns the max (maximum of all measurements) stored by this accumulation.
   *
   * @return the max stored by this accumulation.
   */
  abstract double getMax();

  final DoubleSummaryPointData toPoint(long startEpochNanos, long epochNanos, Attributes labels) {
    return DoubleSummaryPointData.create(
        startEpochNanos,
        epochNanos,
        labels,
        getCount(),
        getSum(),
        Arrays.asList(
            ValueAtPercentile.create(0.0, getMin()), ValueAtPercentile.create(100.0, getMax())));
  }
}
