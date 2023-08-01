/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;

/**
 * Customize the {@link io.opentelemetry.sdk.metrics.export.MetricReader} cardinality limit as a
 * function of {@link InstrumentType}. Register via {@link
 * SdkMeterProviderUtil#registerMetricReaderWithCardinalitySelector(SdkMeterProviderBuilder,
 * MetricReader, CardinalityLimitSelector)}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
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
