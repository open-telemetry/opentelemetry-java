/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import java.util.Collections;

/**
 * A simple, autovalue implementation of {@link ExponentialHistogramData}. For more detailed javadoc
 * on the type, see {@link ExponentialHistogramData}.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#exponentialhistogram
 */
@AutoValue
public abstract class DoubleExponentialHistogramData implements ExponentialHistogramData {

  public static final DoubleExponentialHistogramData EMPTY =
      create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

  DoubleExponentialHistogramData() {}

  /**
   * Create a DoubleExponentialHistogramData.
   *
   * @return a DoubleExponentialHistogramData
   */
  public static DoubleExponentialHistogramData create(
      AggregationTemporality temporality, Collection<ExponentialHistogramPointData> points) {
    return new AutoValue_DoubleExponentialHistogramData(temporality, points);
  }

  @Override
  public abstract AggregationTemporality getAggregationTemporality();

  @Override
  public abstract Collection<ExponentialHistogramPointData> getPoints();
}
