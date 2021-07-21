/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * SummaryPoint is a single data point that summarizes the values in a time series of numeric
 * values.
 */
@Immutable
@AutoValue
public abstract class DoubleSummaryPointData implements PointData {
  /**
   * Creates a {@link DoubleSummaryPointData}.
   *
   * @param startEpochNanos (optional) The starting time for the period where this point was
   *     sampled.
   * @param epochNanos The ending time for the period when this value was sampled.
   * @param attributes The set of attributes associated with this point.
   * @param count The number of measurements being sumarized.
   * @param sum The sum of measuremnts being sumarized.
   * @param percentileValues Calculations of percentile values from measurements.
   */
  public static DoubleSummaryPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      long count,
      double sum,
      List<ValueAtPercentile> percentileValues) {
    return new AutoValue_DoubleSummaryPointData(
        startEpochNanos,
        epochNanos,
        attributes,
        Collections.emptyList(),
        count,
        sum,
        percentileValues);
  }

  DoubleSummaryPointData() {}

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
