/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * A base-2 exponential histogram metric point, as defined by the OpenTelemetry Exponential
 * Histogram specification.
 *
 * <p><i>Note: This is called "ExponentialHistogramData" to reflect which primitives are used to
 * record it, however "ExponentialHistogram" is the equivalent OTLP type.</i>
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/data-model.md#exponentialhistogram">Exponential
 *     Histogram Data Model</a>
 * @see ExponentialHistogramPointData
 * @since 1.23.0
 */
@Immutable
public interface ExponentialHistogramData extends Data<ExponentialHistogramPointData> {

  /**
   * Returns the {@code AggregationTemporality} of this metric.
   *
   * <p>AggregationTemporality describes if the aggregator reports delta changes since last report
   * time, or cumulative changes since a fixed start time.
   *
   * @return the {@code AggregationTemporality} of this metric
   */
  AggregationTemporality getAggregationTemporality();

  /**
   * Returns the collection of {@link ExponentialHistogramPointData} for this histogram.
   *
   * @return the collection of data points for this histogram.
   */
  @Override
  Collection<ExponentialHistogramPointData> getPoints();
}
