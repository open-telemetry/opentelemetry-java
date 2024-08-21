/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import java.util.logging.Logger;

/**
 * Internal builder for configuring OTLP JSON logging exporters.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class InternalMetricBuilder extends InternalBuilder {

  private static final AggregationTemporalitySelector DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR =
      AggregationTemporalitySelector.alwaysCumulative();

  private AggregationTemporality aggregationTemporality = AggregationTemporality.CUMULATIVE;

  private AggregationTemporalitySelector aggregationTemporalitySelector =
      DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR;

  private DefaultAggregationSelector defaultAggregationSelector =
      DefaultAggregationSelector.getDefault();

  public InternalMetricBuilder(Logger logger, String type) {
    super(logger, type);
  }

  public AggregationTemporality getPreferredTemporality() {
    return aggregationTemporality;
  }

  public InternalMetricBuilder setPreferredTemporality(
      AggregationTemporality aggregationTemporality) {
    this.aggregationTemporality = aggregationTemporality;
    return this;
  }

  public AggregationTemporalitySelector getAggregationTemporalitySelector() {
    return aggregationTemporalitySelector;
  }

  public InternalMetricBuilder setAggregationTemporalitySelector(
      AggregationTemporalitySelector aggregationTemporalitySelector) {
    requireNonNull(aggregationTemporalitySelector, "aggregationTemporalitySelector");
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    return this;
  }

  public DefaultAggregationSelector getDefaultAggregationSelector() {
    return defaultAggregationSelector;
  }

  public InternalMetricBuilder setDefaultAggregationSelector(
      DefaultAggregationSelector defaultAggregationSelector) {
    requireNonNull(defaultAggregationSelector, "defaultAggregationSelector");
    this.defaultAggregationSelector = defaultAggregationSelector;
    return this;
  }
}
