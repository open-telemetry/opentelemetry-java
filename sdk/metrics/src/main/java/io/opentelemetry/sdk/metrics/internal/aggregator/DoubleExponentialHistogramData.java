/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.Collection;

@AutoValue
public abstract class DoubleExponentialHistogramData implements ExponentialHistogramData {

  DoubleExponentialHistogramData() {}

  public static DoubleExponentialHistogramData create(
      AggregationTemporality temporality, Collection<ExponentialHistogramPointData> points) {
    return new AutoValue_DoubleExponentialHistogramData(temporality, points);
  }

  @Override
  public abstract AggregationTemporality getAggregationTemporality();

  @Override
  public abstract Collection<ExponentialHistogramPointData> getPoints();
}
