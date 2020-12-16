/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.spi;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.spi.metrics.MeterProviderFactory;

/**
 * {@code MeterProvider} provider implementation for {@link MeterProviderFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}.
 */
public final class MeterProviderFactorySdk implements MeterProviderFactory {

  @Override
  public SdkMeterProvider create() {
    return SdkMeterProvider.builder().build();
  }
}
