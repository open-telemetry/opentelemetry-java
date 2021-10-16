/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.function.Supplier;

final class MinMaxSumCountAggregatorFactory implements AggregatorFactory {
  static final AggregatorFactory INSTANCE = new MinMaxSumCountAggregatorFactory();

  private MinMaxSumCountAggregatorFactory() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      InstrumentDescriptor instrumentDescriptor,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>) new LongMinMaxSumCountAggregator(reservoirSupplier);
      case DOUBLE:
        return (Aggregator<T>) new DoubleMinMaxSumCountAggregator(reservoirSupplier);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
