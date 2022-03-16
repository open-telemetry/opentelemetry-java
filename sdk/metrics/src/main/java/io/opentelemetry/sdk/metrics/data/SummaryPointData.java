/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;

/** A single data point that summarizes the values in a time series of numeric values. */
public interface SummaryPointData extends PointData {
  /** Returns the number of values that are being summarized. */
  long getCount();

  /** Returns the sum of all the values that are being summarized. */
  double getSum();

  /**
   * Returns the values in the summarization. Note: a quantile 0.0 represents the minimum value in
   * the distribution.
   */
  List<ValueAtQuantile> getValues();
}
