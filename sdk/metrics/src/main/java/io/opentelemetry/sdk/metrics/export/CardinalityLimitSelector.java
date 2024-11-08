/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;

/**
 * Customize the {@link io.opentelemetry.sdk.metrics.export.MetricReader} cardinality limit as a
 * function of {@link InstrumentType}. Register via {@link
 * SdkMeterProviderBuilder#registerMetricReader(MetricReader, CardinalityLimitSelector)}.
 *
 * @since 1.44.0
 */
@FunctionalInterface
public interface CardinalityLimitSelector {

  /**
   * The default {@link CardinalityLimitSelector}, allowing each metric to have {@code 2000} points.
   */
  static CardinalityLimitSelector defaultCardinalityLimitSelector() {
    return unused -> MetricStorage.DEFAULT_MAX_CARDINALITY;
  }

  /**
   * Return the default cardinality limit for metrics from instruments of type {@code
   * instrumentType}. The cardinality limit dictates the maximum number of distinct points (or time
   * series) for the metric.
   */
  int getCardinalityLimit(InstrumentType instrumentType);
}
