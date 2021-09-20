/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * An exponential histogram metric point, as defined by the OpenTelemetry Exponential Histogram.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#exponentialhistogram
 *
 * <p><i>Note: This is called "DoubleExponentialHistogramData" to reflect which primitives are used
 * to record it, however "ExponentialHistogram" is the equivalent OTLP type.</i>
 */
@Immutable
@AutoValue
public abstract class DoubleExponentialHistogramData
    implements Data<DoubleExponentialHistogramPointData> {
  DoubleExponentialHistogramData() {}

  public static DoubleExponentialHistogramData create(
      AggregationTemporality temporality, Collection<DoubleExponentialHistogramPointData> points) {
    return new AutoValue_DoubleExponentialHistogramData(temporality, points);
  }

  /**
   * Returns the {@code AggregationTemporality} of this metric.
   *
   * <p>AggregationTemporality describes if the aggregator reports delta changes since last report
   * time, or cumulative changes since a fixed start time.
   *
   * @return the {@code AggregationTemporality} of this metric
   */
  public abstract AggregationTemporality getAggregationTemporality();

  @Override
  public abstract Collection<DoubleExponentialHistogramPointData> getPoints();
}
