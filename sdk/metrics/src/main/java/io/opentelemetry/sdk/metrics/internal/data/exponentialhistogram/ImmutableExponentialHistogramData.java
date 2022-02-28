/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
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
public abstract class ImmutableExponentialHistogramData implements ExponentialHistogramData {

  private static final ExponentialHistogramData EMPTY =
      ExponentialHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

  public static ExponentialHistogramData empty() {
    return EMPTY;
  }

  /**
   * Create a DoubleExponentialHistogramData.
   *
   * @return a DoubleExponentialHistogramData
   */
  public static ExponentialHistogramData create(
      AggregationTemporality temporality, Collection<ExponentialHistogramPointData> points) {
    return new AutoValue_ImmutableExponentialHistogramData(temporality, points);
  }

  ImmutableExponentialHistogramData() {}

  @Override
  public abstract AggregationTemporality getAggregationTemporality();

  @Override
  public abstract Collection<ExponentialHistogramPointData> getPoints();
}
