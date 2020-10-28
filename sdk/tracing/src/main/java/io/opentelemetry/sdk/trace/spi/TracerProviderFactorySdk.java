/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.spi;

import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.spi.TracerProviderFactory;
import io.opentelemetry.sdk.trace.TracerSdkProvider;

/** SDK implementation of the TracerProviderFactory for SPI. */
public final class TracerProviderFactorySdk implements TracerProviderFactory {

  @Override
  public TracerProvider create() {
    return TracerSdkProvider.builder().build();
  }
}
