/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#LONG_SUM} or {@link MetricDataType#DOUBLE_SUM} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface SumData<T extends PointData> extends Data<T> {
  /** Returns "true" if the sum is monotonic. */
  boolean isMonotonic();

  /** Returns the sum {@link AggregationTemporality}. */
  AggregationTemporality getAggregationTemporality();
}
