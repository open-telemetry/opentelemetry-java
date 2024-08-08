/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.internal;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * File configuration SPI implementation for {@link LoggingSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ConsoleSpanExporterComponentProvider implements ComponentProvider<SpanExporter> {

  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "console";
  }

  @Override
  public SpanExporter create(StructuredConfigProperties config) {
    return LoggingSpanExporter.create();
  }
}
