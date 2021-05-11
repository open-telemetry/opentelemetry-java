/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;

final class SumAggregatorFactory implements AggregatorFactory {

  private final AggregationTemporality temporality;

  SumAggregatorFactory(AggregationTemporality temporality) {
    this.temporality = temporality;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor) {
    switch (descriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>)
            new LongSumAggregator(resource, instrumentationLibraryInfo, descriptor, temporality);
      case DOUBLE:
        return (Aggregator<T>)
            new DoubleSumAggregator(resource, instrumentationLibraryInfo, descriptor, temporality);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
