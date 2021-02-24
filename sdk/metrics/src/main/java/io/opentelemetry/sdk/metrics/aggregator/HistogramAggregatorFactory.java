/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.resources.Resource;

final class HistogramAggregatorFactory implements AggregatorFactory {
  private final ImmutableDoubleArray boundaries;
  private final boolean stateful;

  HistogramAggregatorFactory(double[] boundaries, boolean stateful) {
    this.boundaries = ImmutableDoubleArray.copyOf(boundaries);
    this.stateful = stateful;

    for (int i = 1; i < this.boundaries.length(); ++i) {
      if (Double.compare(this.boundaries.get(i - 1), this.boundaries.get(i)) >= 0) {
        throw new IllegalArgumentException(
            "invalid bucket boundary: "
                + this.boundaries.get(i - 1)
                + " >= "
                + this.boundaries.get(i));
      }
    }
    if (this.boundaries.length() > 0) {
      if (this.boundaries.get(0) == Double.NEGATIVE_INFINITY) {
        throw new IllegalArgumentException("invalid bucket boundary: -Inf");
      }
      if (this.boundaries.get(this.boundaries.length() - 1) == Double.POSITIVE_INFINITY) {
        throw new IllegalArgumentException("invalid bucket boundary: +Inf");
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor) {
    switch (descriptor.getValueType()) {
      case LONG:
      case DOUBLE:
        return (Aggregator<T>)
            new DoubleHistogramAggregator(
                resource, instrumentationLibraryInfo, descriptor, this.boundaries, this.stateful);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
