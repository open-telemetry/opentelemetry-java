/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#HISTOGRAM} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface HistogramData extends Data<HistogramPointData> {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static HistogramData create(
      AggregationTemporality temporality, Collection<HistogramPointData> points) {
    return ImmutableHistogramData.create(temporality, points);
  }

  /** Returns the histogram {@link AggregationTemporality}. */
  AggregationTemporality getAggregationTemporality();

  @Override
  Collection<HistogramPointData> getPoints();
}
