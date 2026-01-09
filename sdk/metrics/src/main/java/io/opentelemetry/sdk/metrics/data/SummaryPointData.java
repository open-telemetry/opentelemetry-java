/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Point data for {@link SummaryData}.
 *
 * @since 1.14.0
 */
@Immutable
public interface SummaryPointData extends PointData {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static SummaryPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      long count,
      double sum,
      List<ValueAtQuantile> percentileValues) {
    return ImmutableSummaryPointData.create(
        startEpochNanos, epochNanos, attributes, count, sum, percentileValues);
  }

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
