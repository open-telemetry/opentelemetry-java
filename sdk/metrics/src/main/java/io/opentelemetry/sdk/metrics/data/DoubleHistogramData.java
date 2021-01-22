/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class DoubleHistogramData implements Data<DoubleHistogramPointData> {
  DoubleHistogramData() {}

  public static DoubleHistogramData create(
      AggregationTemporality temporality, Collection<DoubleHistogramPointData> points) {
    return new AutoValue_DoubleHistogramData(temporality, points);
  }

  /**
   * Returns the {@code AggregationTemporality} of this metric,
   *
   * <p>AggregationTemporality describes if the aggregator reports delta changes since last report
   * time, or cumulative changes since a fixed start time.
   *
   * @return the {@code AggregationTemporality} of this metric
   */
  public abstract AggregationTemporality getAggregationTemporality();

  @Override
  public abstract Collection<DoubleHistogramPointData> getPoints();
}
