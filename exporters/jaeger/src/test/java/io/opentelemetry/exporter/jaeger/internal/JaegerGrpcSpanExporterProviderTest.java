/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Testing deprecated code
class JaegerGrpcSpanExporterProviderTest {

  private static final JaegerGrpcSpanExporterProvider provider =
      new JaegerGrpcSpanExporterProvider();

  @Test
  void getName() {
    assertThat(provider.getName()).isEqualTo("jaeger");
  }

  @Test
  void createExporter_Default() {
    try (SpanExporter spanExporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap()))) {
      assertThat(spanExporter)
          .isInstanceOf(io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter.class);
      assertThat(spanExporter)
          .extracting("delegate.grpcSender")
          .extracting("client")
          .extracting("callTimeoutMillis")
          .isEqualTo(10000);
      assertThat(spanExporter)
          .extracting("delegate.grpcSender")
          .extracting("url")
          .isEqualTo(
              HttpUrl.get("http://localhost:14250/jaeger.api_v2.CollectorService/PostSpans"));
    }
  }

  @Test
  void createExporter_WithConfiguration() {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.jaeger.endpoint", "http://endpoint:8080");
    config.put("otel.exporter.jaeger.timeout", "1s");

    try (SpanExporter spanExporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(spanExporter)
          .isInstanceOf(io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter.class);
      assertThat(spanExporter)
          .extracting("delegate.grpcSender")
          .extracting("client")
          .extracting("callTimeoutMillis")
          .isEqualTo(1000);
      assertThat(spanExporter)
          .extracting("delegate.grpcSender")
          .extracting("url")
          .isEqualTo(HttpUrl.get("http://endpoint:8080/jaeger.api_v2.CollectorService/PostSpans"));
    }
  }
}
