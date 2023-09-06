/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import static io.opentelemetry.sdk.metrics.export.MemoryMode.IMMUTABLE_DATA;

import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MemoryMode;

public final class InMemoryMetricReaderBuilder {
  private AggregationTemporalitySelector aggregationTemporalitySelector =
      AggregationTemporalitySelector.alwaysCumulative();
  private DefaultAggregationSelector defaultAggregationSelector =
      DefaultAggregationSelector.getDefault();
  private MemoryMode memoryMode = IMMUTABLE_DATA;

  /**
   * Creates an {@link InMemoryMetricReaderBuilder} with defaults.
   *
   * <p>Creates a builder with always-cumulative {@link AggregationTemporalitySelector}, default
   * {@link DefaultAggregationSelector} and {@link MemoryMode#IMMUTABLE_DATA} {@link MemoryMode}
   */
  InMemoryMetricReaderBuilder() {}

  public InMemoryMetricReaderBuilder setAggregationTemporalitySelector(
      AggregationTemporalitySelector aggregationTemporalitySelector) {
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    return this;
  }

  /**
   * Sets the {@link DefaultAggregationSelector}
   *
   * @param defaultAggregationSelector the {@link DefaultAggregationSelector} to set
   * @return this {@link InMemoryMetricReaderBuilder}
   */
  @SuppressWarnings("unused")
  public InMemoryMetricReaderBuilder setDefaultAggregationSelector(
      DefaultAggregationSelector defaultAggregationSelector) {
    this.defaultAggregationSelector = defaultAggregationSelector;
    return this;
  }

  /**
   * Sets the {@link MemoryMode}.
   *
   * @param memoryMode the {@link MemoryMode} to set
   * @return this {@link InMemoryMetricReaderBuilder}
   */
  public InMemoryMetricReaderBuilder setMemoryMode(MemoryMode memoryMode) {
    this.memoryMode = memoryMode;
    return this;
  }

  public InMemoryMetricReader build() {
    return new InMemoryMetricReader(
        aggregationTemporalitySelector, defaultAggregationSelector, memoryMode);
  }
}
