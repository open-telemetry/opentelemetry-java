/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#LONG_SUM} or {@link MetricDataType#DOUBLE_SUM} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface SumData<T extends PointData> extends Data<T> {

  /**
   * Creates a new instance of {@link SumData}.
   *
   * @param isMonotonic {@code true} if the sum is monotonic.
   * @param temporality the aggregation temporality of the sum data
   * @param points the collection of sum point data
   * @return a new instance of {@link SumData}
   */
  static <T extends PointData> ImmutableSumData<T> create(
      boolean isMonotonic, AggregationTemporality temporality, Collection<T> points) {
    return ImmutableSumData.create(isMonotonic, temporality, points);
  }

  /** Returns "true" if the sum is monotonic. */
  boolean isMonotonic();

  /** Returns the sum {@link AggregationTemporality}. */
  AggregationTemporality getAggregationTemporality();
}
