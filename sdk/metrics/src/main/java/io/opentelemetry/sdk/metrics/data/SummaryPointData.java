/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Point data for {@link SummaryData}.
 *
 * @since 1.14.0
 */
@Immutable
public interface SummaryPointData extends PointData {
  /** Returns the count of measurements. */
  long getCount();

  /** Returns the sum of measurements. */
  double getSum();

  /**
   * Returns the list of values at different quantiles in the distribution of measurements.
   *
   * <p>Note: a quantile 0.0 represents the minimum value in the distribution; a quantile 1.0
   * represents the maximum value in the distribution.
   */
  List<ValueAtQuantile> getValues();
}
