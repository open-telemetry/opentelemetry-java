/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.trace;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * {@link SpanExporter} SPI implementation for {@link OtlpStdoutSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutSpanExporterProvider implements ConfigurableSpanExporterProvider {
  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    return OtlpStdoutSpanExporter.create();
  }

  @Override
  public String getName() {
    return "otlp-stdout";
  }
}
