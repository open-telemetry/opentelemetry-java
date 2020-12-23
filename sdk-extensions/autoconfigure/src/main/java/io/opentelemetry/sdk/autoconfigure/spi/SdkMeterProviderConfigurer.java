/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;

public interface SdkMeterProviderConfigurer {
  void configure(SdkMeterProviderBuilder meterProvider);
}
