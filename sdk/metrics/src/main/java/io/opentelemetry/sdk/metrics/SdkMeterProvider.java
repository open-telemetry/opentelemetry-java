/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;

/**
 * {@code SdkMeterProvider} provides SDK extensions for {@link MeterProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.api.OpenTelemetry}.
 */
public interface SdkMeterProvider extends MeterProvider {

  /** Forces metric readers to immediately read metrics, if able. */
  CompletableResultCode forceFlush();

  /** Shuts down metric collection and all associated metric readers. */
  CompletableResultCode close();

  /**
   * Returns a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   *
   * @return a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   */
  static SdkMeterProviderBuilder builder() {
    return new SdkMeterProviderBuilder();
  }
}
