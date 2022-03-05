/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;

/**
 * A histogram metric recording.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#histogram
 */
public interface HistogramData extends Data<HistogramPointData> {
  /**
   * Returns the {@code AggregationTemporality} of this metric,
   *
   * <p>AggregationTemporality describes if the aggregator reports delta changes since last report
   * time, or cumulative changes since a fixed start time.
   *
   * @return the {@code AggregationTemporality} of this metric
   */
  AggregationTemporality getAggregationTemporality();

  @Override
  Collection<HistogramPointData> getPoints();
}
