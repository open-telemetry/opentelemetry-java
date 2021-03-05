/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.resources.Resource;

final class SumAggregatorFactory implements AggregatorFactory {
  private final boolean alwaysCumulative;

  SumAggregatorFactory(boolean alwaysCumulative) {
    this.alwaysCumulative = alwaysCumulative;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor) {
    boolean stateful = alwaysCumulative;
    if (descriptor.getType() == InstrumentType.SUM_OBSERVER
        || descriptor.getType() == InstrumentType.UP_DOWN_SUM_OBSERVER) {
      stateful = false;
    }
    switch (descriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>)
            new LongSumAggregator(resource, instrumentationLibraryInfo, descriptor, stateful);
      case DOUBLE:
        return (Aggregator<T>)
            new DoubleSumAggregator(resource, instrumentationLibraryInfo, descriptor, stateful);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
