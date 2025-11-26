/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;

/**
 * Declarative configuration SPI implementation for {@link ZipkinSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ZipkinSpanExporterComponentProvider implements ComponentProvider {
  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "zipkin";
  }

  @Override
  public SpanExporter create(DeclarativeConfigProperties config) {
    ZipkinSpanExporterBuilder builder = ZipkinSpanExporter.builder();

    String endpoint = config.getString("endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    Long timeoutMs = config.getLong("timeout");
    if (timeoutMs != null) {
      builder.setReadTimeout(Duration.ofMillis(timeoutMs));
    }

    return builder.build();
  }
}
