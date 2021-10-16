/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.function.Supplier;

final class SumAggregatorFactory implements AggregatorFactory {
  static final AggregatorFactory INSTANCE = new SumAggregatorFactory();

  private SumAggregatorFactory() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      InstrumentDescriptor instrumentDescriptor, Supplier<ExemplarReservoir> reservoirFactory) {
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>) new LongSumAggregator(instrumentDescriptor, reservoirFactory);
      case DOUBLE:
        return (Aggregator<T>) new DoubleSumAggregator(instrumentDescriptor, reservoirFactory);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
