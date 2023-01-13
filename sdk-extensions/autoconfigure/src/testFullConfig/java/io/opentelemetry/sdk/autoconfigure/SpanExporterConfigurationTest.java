/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanExporterConfigurationTest {

  @Test
  void configureExporter_KnownSpiExportersOnClasspath() {
    NamedSpiManager<SpanExporter> spiExportersManager =
        SpanExporterConfiguration.spanExporterSpiManager(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            SpanExporterConfigurationTest.class.getClassLoader());

    assertThat(SpanExporterConfiguration.configureExporter("jaeger", spiExportersManager))
        .isInstanceOf(JaegerGrpcSpanExporter.class);
    assertThat(SpanExporterConfiguration.configureExporter("logging", spiExportersManager))
        .isInstanceOf(LoggingSpanExporter.class);
    assertThat(SpanExporterConfiguration.configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(OtlpJsonLoggingSpanExporter.class);
    assertThat(SpanExporterConfiguration.configureExporter("otlp", spiExportersManager))
        .isInstanceOf(OtlpGrpcSpanExporter.class);
    assertThat(SpanExporterConfiguration.configureExporter("zipkin", spiExportersManager))
        .isInstanceOf(ZipkinSpanExporter.class);
  }

  @Test
  void configureOtlpSpansUnsupportedProtocol() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.exporter.otlp.protocol", "foo"));
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp",
                    SpanExporterConfiguration.spanExporterSpiManager(
                        config, SpanExporterConfigurationTest.class.getClassLoader())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP traces protocol: foo");
  }

  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureOtlpTimeout() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.exporter.otlp.timeout", "10"));
    try (SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            SpanExporterConfiguration.spanExporterSpiManager(
                config, SpanExporterConfigurationTest.class.getClassLoader()))) {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OtlpGrpcSpanExporter.class,
              otlp ->
                  assertThat(otlp).extracting("delegate.client.callTimeoutMillis").isEqualTo(10));
    }
  }
}
