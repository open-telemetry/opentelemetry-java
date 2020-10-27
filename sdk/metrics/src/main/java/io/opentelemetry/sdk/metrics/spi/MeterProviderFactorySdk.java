/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.spi;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.spi.MeterProviderFactory;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;

/**
 * {@code MeterProvider} provider implementation for {@link MeterProviderFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}.
 */
public final class MeterProviderFactorySdk implements MeterProviderFactory {

  @Override
  public MeterSdkProvider create() {
    return MeterSdkProvider.builder().build();
  }
}
