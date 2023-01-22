/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.provider.TestConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ConfigurableMetricExporterTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configureExporter_spiExporter() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));

    try (MetricExporter metricExporter =
        MetricExporterConfiguration.configureExporter(
            "testExporter",
            MetricExporterConfiguration.metricExporterSpiManager(
                config, ConfigurableMetricExporterTest.class.getClassLoader()))) {
      assertThat(metricExporter)
          .isInstanceOf(TestConfigurableMetricExporterProvider.TestMetricExporter.class)
          .extracting("config")
          .isSameAs(config);
    }
  }

  @Test
  void configureExporter_emptyClassLoader() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "testExporter",
                    MetricExporterConfiguration.metricExporterSpiManager(
                        DefaultConfigProperties.createForTest(Collections.emptyMap()),
                        new URLClassLoader(new URL[] {}, null))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
  }

  @Test
  void configureExporter_exporterNotFound() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "catExporter",
                    MetricExporterConfiguration.metricExporterSpiManager(
                        DefaultConfigProperties.createForTest(Collections.emptyMap()),
                        ConfigurableMetricExporterTest.class.getClassLoader())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }

  @Test
  void configureMetricReaders_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.metrics.exporter", "otlp,none"));
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                MeterProviderConfiguration.configureMetricReaders(
                    config,
                    ConfigurableMetricExporterTest.class.getClassLoader(),
                    (a, unused) -> a,
                    closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.metrics.exporter contains none along with other exporters");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureMetricReaders_defaultExporter() {
    ConfigProperties config = DefaultConfigProperties.createForTest(Collections.emptyMap());
    List<Closeable> closeables = new ArrayList<>();

    List<MetricReader> metricReaders =
        MeterProviderConfiguration.configureMetricReaders(
            config,
            MeterProviderConfiguration.class.getClassLoader(),
            (metricExporter, unused) -> metricExporter,
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(metricReaders)
        .satisfiesExactly(
            metricReader ->
                assertThat(metricReader)
                    .isInstanceOf(PeriodicMetricReader.class)
                    .extracting("exporter")
                    .isInstanceOf(OtlpGrpcMetricExporter.class));
    assertThat(closeables)
        .hasExactlyElementsOfTypes(OtlpGrpcMetricExporter.class, PeriodicMetricReader.class);
  }

  @Test
  void configureMetricReaders_multipleExporters() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.metrics.exporter", "otlp,logging"));
    List<Closeable> closeables = new ArrayList<>();

    List<MetricReader> metricReaders =
        MeterProviderConfiguration.configureMetricReaders(
            config,
            MeterProviderConfiguration.class.getClassLoader(),
            (metricExporter, unused) -> metricExporter,
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(metricReaders).hasSize(2).hasOnlyElementsOfType(PeriodicMetricReader.class);
    assertThat(closeables)
        .hasSize(4)
        .hasOnlyElementsOfTypes(
            PeriodicMetricReader.class, LoggingMetricExporter.class, OtlpGrpcMetricExporter.class);
  }
}
