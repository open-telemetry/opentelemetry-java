/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Configures the {@link io.opentelemetry.sdk.metrics.data.MetricData} output for a {@link View}.
 */
public interface MetricOutputConfigurationBuilder {
  /** Set the name for the resulting metric. */
  MetricOutputConfigurationBuilder setName(String name);
  /** Set the description for the resulting metric. */
  MetricOutputConfigurationBuilder setDescription(String description);
  /** The list of attribute keys that will be output. */
  MetricOutputConfigurationBuilder setAttributeKeyFilter(AttributeKey<?>... keys);
  /** Set the list of keys to be pulled from {@link io.opentelemetry.api.baggage.Baggage}. */
  MetricOutputConfigurationBuilder setExtraDimensions(String... keys);
  /** Output a Sum metric point. Monotonicity will be determined from the instrument type. */
  MetricOutputConfigurationBuilder aggregateAsSum();
  /** Output Gauge metric points which keep the last seen measurement. */
  MetricOutputConfigurationBuilder aggregateAsLastValue();
  /** Output Histogram metric points using SDK provided boundaries. */
  MetricOutputConfigurationBuilder aggregateAsHistogram();
  /** Output Histogram metric points using the given boundaries. */
  MetricOutputConfigurationBuilder aggregateAsHistogramWithFixedBoundaries(double[] boundaries);

  // Temporary.
  MetricOutputConfigurationBuilder withDeltaAggregation();
  /** Returns the metric output. */
  MetricOutputConfiguration build();
}
