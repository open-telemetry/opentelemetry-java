/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

/** Last-value aggregation configuration. */
class LastValueAggregation extends Aggregation {

  static final Aggregation INSTANCE = new LastValueAggregation();

  private LastValueAggregation() {}

  @Override
  public <T> Aggregator<T> createAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      ExemplarFilter exemplarFilter) {
    // For the initial version we do not sample exemplars on gauges.
    return AggregatorFactory.lastValue()
        .create(
            resource,
            instrumentationLibraryInfo,
            instrumentDescriptor,
            metricDescriptor,
            ExemplarReservoir::noSamples);
  }

  @Override
  public String toString() {
    return "lastValue";
  }
}
