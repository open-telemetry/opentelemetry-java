/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.spi;

import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.spi.trace.TracerProviderFactory;

/** SDK implementation of the {@link TracerProviderFactory} for SPI. */
public final class SdkTracerProviderFactory implements TracerProviderFactory {

  @Override
  public TracerProvider create() {
    return SdkTracerProvider.builder().build();
  }
}
