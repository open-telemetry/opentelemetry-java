/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

final class MinMaxSumCountAggregatorFactory implements AggregatorFactory {
  static final AggregatorFactory INSTANCE = new MinMaxSumCountAggregatorFactory();

  private MinMaxSumCountAggregatorFactory() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor) {
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>)
            new LongMinMaxSumCountAggregator(
                resource, instrumentationLibraryInfo, metricDescriptor);
      case DOUBLE:
        return (Aggregator<T>)
            new DoubleMinMaxSumCountAggregator(
                resource, instrumentationLibraryInfo, metricDescriptor);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
