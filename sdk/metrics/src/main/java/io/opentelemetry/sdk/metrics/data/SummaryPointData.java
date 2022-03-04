/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;

/** A single data point that summarizes the values in a time series of numeric values. */
public interface SummaryPointData extends PointData {
  /**
   * The number of values that are being summarized.
   *
   * @return the number of values that are being summarized.
   */
  long getCount();

  /**
   * The sum of all the values that are being summarized.
   *
   * @return the sum of the values that are being summarized.
   */
  double getSum();

  /**
   * Percentile values in the summarization. Note: a percentile 0.0 represents the minimum value in
   * the distribution.
   *
   * @return the percentiles values.
   */
  List<ValueAtPercentile> getPercentileValues();
}
