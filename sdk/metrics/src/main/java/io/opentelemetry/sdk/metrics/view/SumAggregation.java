/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

/** A sum aggregation configuration. */
class SumAggregation extends Aggregation {
  private final AggregationTemporality temporality;

  SumAggregation(AggregationTemporality temporality) {
    this.temporality = temporality;
  }

  /** Returns the configured temporality for the sum aggregation. */
  public AggregationTemporality getTemporality() {
    return temporality;
  }

  @Override
  public <T> Aggregator<T> createAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      ExemplarFilter exemplarFilter) {
    return AggregatorFactory.sum(temporality)
        .create(
            resource,
            instrumentationLibraryInfo,
            instrumentDescriptor,
            metricDescriptor,
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
    return "sum(" + temporality + ")";
  }
}
