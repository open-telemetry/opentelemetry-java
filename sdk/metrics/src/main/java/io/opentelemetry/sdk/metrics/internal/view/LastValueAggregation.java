/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;

/**
 * Last-value aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LastValueAggregation implements Aggregation, AggregatorFactory {

  private static final Aggregation INSTANCE = new LastValueAggregation();

  public static Aggregation getInstance() {
    return INSTANCE;
  }

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
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    return instrumentDescriptor.getType() == InstrumentType.OBSERVABLE_GAUGE;
  }

  @Override
  public String toString() {
    return "LastValueAggregation";
  }
}
