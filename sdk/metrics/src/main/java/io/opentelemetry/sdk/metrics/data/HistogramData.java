/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#HISTOGRAM} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface HistogramData extends Data<HistogramPointData> {
  /** Returns the histogram {@link AggregationTemporality}. */
  AggregationTemporality getAggregationTemporality();

  @Override
  Collection<HistogramPointData> getPoints();
}
