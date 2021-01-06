/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.accumulation;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Arrays;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class MinMaxSumCountAccumulation implements Accumulation {
  /**
   * Creates a new {@link MinMaxSumCountAccumulation} with the given values.
   *
   * @param count the number of measurements.
   * @param sum the sum of the measurements.
   * @param min the min value out of all measurements.
   * @param max the max value out of all measurements.
   * @return a new {@link MinMaxSumCountAccumulation} with the given values.
   */
  public static MinMaxSumCountAccumulation create(long count, double sum, double min, double max) {
    return new AutoValue_MinMaxSumCountAccumulation(count, sum, min, max);
  }

  MinMaxSumCountAccumulation() {}

  /**
   * Returns the count (number of measurements) stored by this {@link Accumulation}.
   *
   * @return the count stored by this {@link Accumulation}.
   */
  public abstract long getCount();

  /**
   * Returns the sum (sum of measurements) stored by this {@link Accumulation}.
   *
   * @return the sum stored by this {@link Accumulation}.
   */
  public abstract double getSum();

  /**
   * Returns the min (minimum of all measurements) stored by this {@link Accumulation}.
   *
   * @return the min stored by this {@link Accumulation}.
   */
  public abstract double getMin();

  /**
   * Returns the max (maximum of all measurements) stored by this {@link Accumulation}.
   *
   * @return the max stored by this {@link Accumulation}.
   */
  public abstract double getMax();

  @Override
  public MetricData.DoubleSummaryPoint toPoint(
      long startEpochNanos, long epochNanos, Labels labels) {
    return MetricData.DoubleSummaryPoint.create(
        startEpochNanos,
        epochNanos,
        labels,
        getCount(),
        getSum(),
        Arrays.asList(
            MetricData.ValueAtPercentile.create(0.0, getMin()),
            MetricData.ValueAtPercentile.create(100.0, getMax())));
  }
}
