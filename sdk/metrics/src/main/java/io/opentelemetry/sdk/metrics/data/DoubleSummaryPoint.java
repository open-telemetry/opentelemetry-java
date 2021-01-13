/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * SummaryPoint is a single data point that summarizes the values in a time series of numeric
 * values.
 */
@Immutable
@AutoValue
public abstract class DoubleSummaryPoint implements Point {
  public static DoubleSummaryPoint create(
      long startEpochNanos,
      long epochNanos,
      Labels labels,
      long count,
      double sum,
      List<ValueAtPercentile> percentileValues) {
    return new AutoValue_DoubleSummaryPoint(
        startEpochNanos, epochNanos, labels, count, sum, percentileValues);
  }

  DoubleSummaryPoint() {}

  /**
   * The number of values that are being summarized.
   *
   * @return the number of values that are being summarized.
   */
  public abstract long getCount();

  /**
   * The sum of all the values that are being summarized.
   *
   * @return the sum of the values that are being summarized.
   */
  public abstract double getSum();

  /**
   * Percentile values in the summarization. Note: a percentile 0.0 represents the minimum value in
   * the distribution.
   *
   * @return the percentiles values.
   */
  public abstract List<ValueAtPercentile> getPercentileValues();
}
