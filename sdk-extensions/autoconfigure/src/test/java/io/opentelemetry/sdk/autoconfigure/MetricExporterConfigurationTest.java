/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.MetricExporterConfiguration.configureExporter;
import static io.opentelemetry.sdk.autoconfigure.MetricExporterConfiguration.configureMetricReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetricExporterConfigurationTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createForTest(Collections.emptyMap());

  private final SpiHelper spiHelper =
      SpiHelper.create(MetricExporterConfigurationTest.class.getClassLoader());

  @Test
  void configureExporter_UnknownExporter() {
    NamedSpiManager<MetricExporter> spiExportersManager =
        MetricExporterConfiguration.metricExporterSpiManager(EMPTY, spiHelper);

    assertThat(configureExporter("foo", spiExportersManager)).isNull();
  }

  @Test
  void configureExporter_KnownSpiExportersNotOnClasspath() {
    NamedSpiManager<MetricExporter> spiExportersManager =
        MetricExporterConfiguration.metricExporterSpiManager(EMPTY, spiHelper);

    assertThatThrownBy(() -> configureExporter("logging", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.metrics.exporter set to \"logging\" but opentelemetry-exporter-logging"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.metrics.exporter set to \"logging-otlp\" but opentelemetry-exporter-logging-otlp"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("otlp", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.metrics.exporter set to \"otlp\" but opentelemetry-exporter-otlp"
                + " not found on classpath. Make sure to add it as a dependency.");
  }

  @Test
  void configureMetricReader_UnknownExporter() {
    NamedSpiManager<MetricReader> spiMetricReadersManager =
        MetricExporterConfiguration.metricReadersSpiManager(EMPTY, spiHelper);

    assertThat(configureMetricReader("foo", spiMetricReadersManager)).isNull();
  }

  @Test
  void configureMetricReader_KnownSpiExportersNotOnClasspath() {
    NamedSpiManager<MetricReader> spiMetricReadersManager =
        MetricExporterConfiguration.metricReadersSpiManager(EMPTY, spiHelper);

    assertThatThrownBy(() -> configureMetricReader("prometheus", spiMetricReadersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.metrics.exporter set to \"prometheus\" but opentelemetry-exporter-prometheus"
                + " not found on classpath. Make sure to add it as a dependency.");
  }
}
