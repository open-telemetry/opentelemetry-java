/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;

/** Configuration representing no aggregation. */
final class DropAggregation implements Aggregation, AggregatorFactory {

  static final Aggregation INSTANCE = new DropAggregation();

  private DropAggregation() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return (Aggregator<T>) Aggregator.drop();
  }

  @Override
  public String toString() {
    return "DropAggregation";
  }
}
