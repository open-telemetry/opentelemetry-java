/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.Collection;

public final class DoubleExponentialHistogram implements ExponentialHistogramData {

  private final AggregationTemporality temporality;
  private final Collection<ExponentialHistogramPointData> points;

  DoubleExponentialHistogram(
      AggregationTemporality temporality, Collection<ExponentialHistogramPointData> points) {
    this.temporality = temporality;
    this.points = points;
  }

  @Override
  public AggregationTemporality getAggregationTemporality() {
    return temporality;
  }

  @Override
  public Collection<ExponentialHistogramPointData> getPoints() {
    return points;
  }
}
