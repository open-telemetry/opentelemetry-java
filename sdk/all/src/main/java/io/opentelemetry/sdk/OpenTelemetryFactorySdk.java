/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.spi.OpenTelemetryFactory;

/**
 * Factory SPI implementation to register a {@link OpenTelemetrySdk} as the default {@link
 * OpenTelemetry}.
 */
public class OpenTelemetryFactorySdk implements OpenTelemetryFactory {
  @Override
  public OpenTelemetry create() {
    return OpenTelemetrySdk.builder().build();
  }
}
