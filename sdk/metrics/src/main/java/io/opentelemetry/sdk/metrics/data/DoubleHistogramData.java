/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * A histogram metric point.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#histogram
 *
 * <p><i>Note: This is called "DoubleHistogram" to reflect which primitives are used to record it,
 * however "Histogram" is the equivalent OTLP type.</i>
 */
@Immutable
@AutoValue
public abstract class DoubleHistogramData implements Data<DoubleHistogramPointData> {

  static final DoubleHistogramData DEFAULT =
      DoubleHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

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
