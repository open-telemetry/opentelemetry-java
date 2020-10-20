/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.spi;

import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TracerProviderFactory;

/** SDK implementation of the TracerProviderFactory for SPI. */
public final class TracerProviderFactorySdk implements TracerProviderFactory {

  @Override
  public TracerProvider create() {
    return TracerSdkProvider.builder().build();
  }
}
