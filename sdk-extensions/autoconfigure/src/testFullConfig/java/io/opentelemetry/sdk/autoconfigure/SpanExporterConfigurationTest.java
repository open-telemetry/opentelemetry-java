/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // testing deprecated code
class SpanExporterConfigurationTest {

  private final SpiHelper spiHelper =
      SpiHelper.create(SpanExporterConfigurationTest.class.getClassLoader());

  @Test
  void configureExporter_KnownSpiExportersOnClasspath() {
    NamedSpiManager<SpanExporter> spiExportersManager =
        SpanExporterConfiguration.spanExporterSpiManager(
            DefaultConfigProperties.createFromMap(Collections.emptyMap()), spiHelper);

    assertThat(SpanExporterConfiguration.configureExporter("console", spiExportersManager))
        .isInstanceOf(LoggingSpanExporter.class);
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
        DefaultConfigProperties.createFromMap(
            ImmutableMap.of("otel.exporter.otlp.protocol", "foo"));
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", SpanExporterConfiguration.spanExporterSpiManager(config, spiHelper)))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP traces protocol: foo");
  }

  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureOtlpTimeout() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.exporter.otlp.timeout", "10"));
    try (SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "otlp", SpanExporterConfiguration.spanExporterSpiManager(config, spiHelper))) {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OtlpGrpcSpanExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("delegate.grpcSender.client.callTimeoutMillis")
                      .isEqualTo(10));
    }
  }
}
