/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

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

  abstract long getCount();

  abstract double getSum();

  abstract double getMin();

  abstract double getMax();

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
