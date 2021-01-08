/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.spi;

import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

/** SDK implementation of tracing. */
@SuppressWarnings("deprecation") // Remove after deleting OpenTelemetry SPI
public final class SdkTracerProviderFactory
    implements io.opentelemetry.spi.trace.TracerProviderFactory {

  @Override
  public TracerProvider create() {
    return SdkTracerProvider.builder().build();
  }
}
