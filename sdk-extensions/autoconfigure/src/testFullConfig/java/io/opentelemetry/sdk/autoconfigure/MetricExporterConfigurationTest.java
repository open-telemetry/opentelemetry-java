/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MetricExporterConfigurationTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createForTest(Collections.emptyMap());

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configureReader_PrometheusOnClasspath() {
    List<Closeable> closeables = new ArrayList<>();

    MetricReader reader =
        MetricExporterConfiguration.configureReader(
            "prometheus",
            EMPTY,
            MetricExporterConfigurationTest.class.getClassLoader(),
            (a, b) -> a,
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(reader).isNull();
    assertThat(closeables).isEmpty();
  }

  /**
   * Prometheus uses the {@link AutoConfigurationCustomizerProvider} SPI instead of {@link
   * ConfigurableMetricExporterProvider} because it is implemented as a {@link MetricReader}. While
   * {@link MetricExporterConfiguration} does not load this SPI, the test code lives here alongside
   * tests of the other known SPI metric exporters.
   */
  @Test
  void autoConfiguredOpenTelemetrySdk_PrometheusOnClasspath() {
    Map<String, String> config = new HashMap<>();
    config.put("otel.traces.exporter", "none");
    config.put("otel.metrics.exporter", "prometheus");
    config.put("otel.logs.exporter", "none");

    try (OpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .setConfig(DefaultConfigProperties.createForTest(config))
            .build()
            .getOpenTelemetrySdk()) {
      assertThat(sdk.getSdkMeterProvider())
          .extracting("registeredReaders", as(InstanceOfAssertFactories.list(Object.class)))
          .satisfiesExactly(
              registeredReader ->
                  assertThat(registeredReader)
                      .extracting("metricReader")
                      .isInstanceOf(PrometheusHttpServer.class));
    }
  }

  @Test
  void configureExporter_KnownSpiExportersOnClasspath() {
    NamedSpiManager<MetricExporter> spiExportersManager =
        MetricExporterConfiguration.metricExporterSpiManager(
            EMPTY, ConfigurableMetricExporterTest.class.getClassLoader());

    assertThat(MetricExporterConfiguration.configureExporter("logging", spiExportersManager))
        .isInstanceOf(LoggingMetricExporter.class);
    assertThat(MetricExporterConfiguration.configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(OtlpJsonLoggingMetricExporter.class);
    assertThat(MetricExporterConfiguration.configureExporter("otlp", spiExportersManager))
        .isInstanceOf(OtlpGrpcMetricExporter.class);
  }

  @Test
  void configureOtlpMetricsUnsupportedProtocol() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "otlp",
                    MetricExporterConfiguration.metricExporterSpiManager(
                        DefaultConfigProperties.createForTest(
                            ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                        MetricExporterConfigurationTest.class.getClassLoader())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP metrics protocol: foo");
  }
}
