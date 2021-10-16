/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.function.Supplier;

final class CountAggregatorFactory implements AggregatorFactory {
  static final AggregatorFactory INSTANCE = new CountAggregatorFactory();

  private CountAggregatorFactory() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      InstrumentDescriptor unused, Supplier<ExemplarReservoir> reservoirSupplier) {
    return (Aggregator<T>) new CountAggregator(reservoirSupplier);
  }
}
