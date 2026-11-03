/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import java.util.Objects;

/** Builder for {@link InMemoryMetricExporter}. */
public final class InMemoryMetricExporterBuilder {
  private AggregationTemporalitySelector aggregationTemporalitySelector =
      AggregationTemporalitySelector.alwaysCumulative();

  InMemoryMetricExporterBuilder() {}

  /**
   * Sets the {@link AggregationTemporalitySelector} used by {@link
   * InMemoryMetricExporter#getAggregationTemporality(InstrumentType)}.
   *
   * <p>The default is {@link AggregationTemporalitySelector#alwaysCumulative()}.
   *
   * @param aggregationTemporalitySelector the {@link AggregationTemporalitySelector} to set
   * @return this {@link InMemoryMetricExporterBuilder}
   */
  public InMemoryMetricExporterBuilder setAggregationTemporalitySelector(
      AggregationTemporalitySelector aggregationTemporalitySelector) {
    Objects.requireNonNull(aggregationTemporalitySelector, "aggregationTemporalitySelector");
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    return this;
  }

  /** Constructs an {@link InMemoryMetricExporter} based on the builder's values. */
  public InMemoryMetricExporter build() {
    return new InMemoryMetricExporter(aggregationTemporalitySelector);
  }
}
