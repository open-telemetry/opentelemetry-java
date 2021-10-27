/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;

/** Configuration representing no aggregation. */
class NoAggregation extends Aggregation {

  static final Aggregation INSTANCE = new NoAggregation();

  private NoAggregation() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return (Aggregator<T>) Aggregator.empty();
  }

  @Override
  public String toString() {
    return "NoAggregation";
  }
}
