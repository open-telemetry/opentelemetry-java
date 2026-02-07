/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // testing deprecated code
class ZipkinSpanExporterProviderTest {

  private static final ZipkinSpanExporterProvider provider = new ZipkinSpanExporterProvider();

  @Test
  void getName() {
    assertThat(provider.getName()).isEqualTo("zipkin");
  }

  @Test
  void createExporter_Default() {
    try (SpanExporter spanExporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap()))) {
      assertThat(spanExporter).isInstanceOf(ZipkinSpanExporter.class);
      assertThat(spanExporter)
          .extracting("sender")
          .extracting("delegate")
          .extracting("client")
          .extracting("readTimeoutMillis")
          .isEqualTo(10_000);
      assertThat(spanExporter)
          .extracting("sender")
          .extracting("endpoint")
          .isEqualTo("http://localhost:9411/api/v2/spans");
    }
  }

  @Test
  void createExporter_WithConfiguration() {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.zipkin.endpoint", "http://localhost:8080/spans");
    config.put("otel.exporter.zipkin.timeout", "1s");

    try (SpanExporter spanExporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(spanExporter)
          .isInstanceOf(io.opentelemetry.exporter.zipkin.ZipkinSpanExporter.class);
      assertThat(spanExporter)
          .extracting("sender")
          .extracting("delegate")
          .extracting("client")
          .extracting("readTimeoutMillis")
          .isEqualTo(1000);
      assertThat(spanExporter)
          .extracting("sender")
          .extracting("endpoint")
          .isEqualTo("http://localhost:8080/spans");
    }
  }
}
