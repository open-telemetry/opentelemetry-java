/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;

/** Configuration representing no aggregation. */
class DropAggregation extends Aggregation {

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
