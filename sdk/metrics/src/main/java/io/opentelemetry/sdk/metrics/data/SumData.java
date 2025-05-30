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
   * Create a record.
   *
   * @since 1.50.0
   */
  static SumData<LongPointData> createLongSumData(
      boolean isMonotonic, AggregationTemporality temporality, Collection<LongPointData> points) {
    return ImmutableSumData.create(isMonotonic, temporality, points);
  }

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static SumData<DoublePointData> createDoubleSumData(
      boolean isMonotonic, AggregationTemporality temporality, Collection<DoublePointData> points) {
    return ImmutableSumData.create(isMonotonic, temporality, points);
  }

  /** Returns "true" if the sum is monotonic. */
  boolean isMonotonic();

  /** Returns the sum {@link AggregationTemporality}. */
  AggregationTemporality getAggregationTemporality();
}
