/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.function.Supplier;

final class LastValueAggregatorFactory implements AggregatorFactory {
  static final AggregatorFactory INSTANCE = new LastValueAggregatorFactory();

  private LastValueAggregatorFactory() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      InstrumentDescriptor descriptor, Supplier<ExemplarReservoir> reservoirSupplier) {
    switch (descriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>) new LongLastValueAggregator(reservoirSupplier);
      case DOUBLE:
        return (Aggregator<T>) new DoubleLastValueAggregator(reservoirSupplier);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
