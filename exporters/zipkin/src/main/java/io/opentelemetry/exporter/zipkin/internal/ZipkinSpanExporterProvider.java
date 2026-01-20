/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;

/**
 * {@link SpanExporter} SPI implementation for {@link
 * io.opentelemetry.exporter.zipkin.ZipkinSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("deprecation")
public class ZipkinSpanExporterProvider implements ConfigurableSpanExporterProvider {
  @Override
  public String getName() {
    return "zipkin";
  }

  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder builder =
        io.opentelemetry.exporter.zipkin.ZipkinSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.zipkin.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    Duration timeout = config.getDuration("otel.exporter.zipkin.timeout");
    if (timeout != null) {
      builder.setReadTimeout(timeout);
    }

    return builder.build();
  }
}
