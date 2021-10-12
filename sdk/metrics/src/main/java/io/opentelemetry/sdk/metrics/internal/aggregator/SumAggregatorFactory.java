/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.function.Supplier;

final class SumAggregatorFactory implements AggregatorFactory {

  private final AggregationTemporality temporality;

  SumAggregatorFactory(AggregationTemporality temporality) {
    this.temporality = temporality;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      Supplier<ExemplarReservoir> reservoirFactory) {
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>)
            new LongSumAggregator(
                resource,
                instrumentationLibraryInfo,
                instrumentDescriptor,
                metricDescriptor,
                temporality,
                reservoirFactory);
      case DOUBLE:
        return (Aggregator<T>)
            new DoubleSumAggregator(
                resource,
                instrumentationLibraryInfo,
                instrumentDescriptor,
                metricDescriptor,
                temporality,
                reservoirFactory);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
