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
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import javax.annotation.Nullable;

/** A sum aggregation configuration. */
class SumAggregation extends Aggregation {
  static final SumAggregation DEFAULT = new SumAggregation(null);

  @Nullable
  private final AggregationTemporality temporality;

  SumAggregation(@Nullable AggregationTemporality temporality) {
    this.temporality = temporality;
  }

  @Override
  public AggregationTemporality getConfiguredTemporality() {
    return temporality;
  }

  @Override
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return AggregatorFactory.sum()
        .create(
            instrumentDescriptor,
            () ->
                ExemplarReservoir.filtered(
                    exemplarFilter,
                    ExemplarReservoir.fixedSizeReservoir(
                        Clock.getDefault(),
                        Runtime.getRuntime().availableProcessors(),
                        RandomSupplier.platformDefault())));
  }

  @Override
  public String toString() {
    return "SumAggregation(" + temporality + ")";
  }
}
