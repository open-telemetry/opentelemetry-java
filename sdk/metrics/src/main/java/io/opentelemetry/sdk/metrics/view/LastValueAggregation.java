/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.LongLastValueAggregator;

/** Last-value aggregation configuration. */
class LastValueAggregation extends Aggregation {

  static final Aggregation INSTANCE = new LastValueAggregation();

  private LastValueAggregation() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {

    // For the initial version we do not sample exemplars on gauges.
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>) new LongLastValueAggregator(ExemplarReservoir::noSamples);
      case DOUBLE:
        return (Aggregator<T>) new DoubleLastValueAggregator(ExemplarReservoir::noSamples);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }

  @Override
  public String toString() {
    return "LastValueAggregation";
  }
}
