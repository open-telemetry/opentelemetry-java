/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetricExporterConfigurationTest {

  @Test
  void configureExporter_KnownSpiExportersOnClasspath() {
    NamedSpiManager<MetricExporter> spiExportersManager =
        MetricExporterConfiguration.metricExporterSpiManager(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            ConfigurableMetricExporterTest.class.getClassLoader());

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
