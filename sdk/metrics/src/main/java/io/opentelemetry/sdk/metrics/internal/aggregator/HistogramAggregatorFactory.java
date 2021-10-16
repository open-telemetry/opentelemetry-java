/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.function.Supplier;

final class HistogramAggregatorFactory implements AggregatorFactory {
  private final double[] boundaries;

  HistogramAggregatorFactory(List<Double> boundaries) {
    this.boundaries = ExplicitBucketHistogramUtils.createBoundaryArray(boundaries);

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
      MetricDescriptor metricDescriptor,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
      case DOUBLE:
        return (Aggregator<T>) new DoubleHistogramAggregator(this.boundaries, reservoirSupplier);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
