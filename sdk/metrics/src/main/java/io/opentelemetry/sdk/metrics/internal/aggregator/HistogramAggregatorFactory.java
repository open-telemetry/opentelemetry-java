/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;

final class HistogramAggregatorFactory implements AggregatorFactory {
  private final double[] boundaries;
  private final AggregationTemporality temporality;

  HistogramAggregatorFactory(List<Double> boundaries, AggregationTemporality temporality) {
    this.boundaries = boundaries.stream().mapToDouble(i -> i).toArray();
    this.temporality = temporality;

    for (double v : this.boundaries) {
      if (Double.isNaN(v)) {
        throw new IllegalArgumentException("invalid bucket boundary: NaN");
      }
    }
    for (int i = 1; i < this.boundaries.length; ++i) {
      if (this.boundaries[i - 1] >= this.boundaries[i]) {
        throw new IllegalArgumentException(
            "invalid bucket boundary: " + this.boundaries[i - 1] + " >= " + this.boundaries[i]);
      }
    }
    if (this.boundaries.length > 0) {
      if (this.boundaries[0] == Double.NEGATIVE_INFINITY) {
        throw new IllegalArgumentException("invalid bucket boundary: -Inf");
      }
      if (this.boundaries[this.boundaries.length - 1] == Double.POSITIVE_INFINITY) {
        throw new IllegalArgumentException("invalid bucket boundary: +Inf");
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor) {
    final boolean stateful = this.temporality == AggregationTemporality.CUMULATIVE;
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
      case DOUBLE:
        return (Aggregator<T>)
            new DoubleHistogramAggregator(
                resource, instrumentationLibraryInfo, metricDescriptor, this.boundaries, stateful);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
