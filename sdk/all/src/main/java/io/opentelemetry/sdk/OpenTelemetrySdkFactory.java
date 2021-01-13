/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.OpenTelemetry;

/**
 * Factory SPI implementation to register a {@link OpenTelemetrySdk} as the default {@link
 * OpenTelemetry}.
 */
@SuppressWarnings("deprecation") // Remove after deleting OpenTelemetry SPI
public final class OpenTelemetrySdkFactory implements io.opentelemetry.spi.OpenTelemetryFactory {
  @Override
  public OpenTelemetry create() {
    return OpenTelemetrySdk.builder().build();
  }
}
