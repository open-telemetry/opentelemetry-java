/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.spi.OpenTelemetryFactory;

/**
 * Factory SPI implementation to register a {@link OpenTelemetrySdk} as the default {@link
 * OpenTelemetry}.
 */
public final class OpenTelemetrySdkFactory implements OpenTelemetryFactory {
  @Override
  public OpenTelemetry create() {
    return OpenTelemetrySdk.builder().buildWithoutRegisteringGlobal();
  }
}
