/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.LongSumAggregator;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/** A sum aggregation configuration. */
class SumAggregation extends Aggregation {
  static final SumAggregation DEFAULT = new SumAggregation(null);

  @Nullable private final AggregationTemporality temporality;

  SumAggregation(@Nullable AggregationTemporality temporality) {
    this.temporality = temporality;
  }

  @Override
  public AggregationTemporality getConfiguredTemporality() {
    return temporality;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    Supplier<ExemplarReservoir> reservoirFactory =
        () ->
            ExemplarReservoir.filtered(
                exemplarFilter,
                ExemplarReservoir.fixedSizeReservoir(
                    Clock.getDefault(),
                    Runtime.getRuntime().availableProcessors(),
                    RandomSupplier.platformDefault()));
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>) new LongSumAggregator(instrumentDescriptor, reservoirFactory);
      case DOUBLE:
        return (Aggregator<T>) new DoubleSumAggregator(instrumentDescriptor, reservoirFactory);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }

  @Override
  public String toString() {
    return "SumAggregation(" + temporality + ")";
  }
}
