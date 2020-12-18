/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.spi;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.spi.metrics.MeterProviderFactory;

/** SDK implementation of the {@link MeterProviderFactory} for SPI. */
public final class SdkMeterProviderFactory implements MeterProviderFactory {

  @Override
  public SdkMeterProvider create() {
    return SdkMeterProvider.builder().build();
  }
}
