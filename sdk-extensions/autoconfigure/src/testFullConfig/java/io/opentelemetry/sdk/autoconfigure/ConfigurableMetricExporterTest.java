/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.internal.OtlpMetricExporterProvider;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
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

  private final SpiHelper spiHelper =
      SpiHelper.create(ConfigurableMetricExporterTest.class.getClassLoader());

  @Test
  void configureExporter_spiExporter() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(ImmutableMap.of("test.option", "true"));

    try (MetricExporter metricExporter =
        MetricExporterConfiguration.configureExporter(
            "testExporter",
            MetricExporterConfiguration.metricExporterSpiManager(config, spiHelper))) {
      assertThat(metricExporter)
          .isInstanceOf(TestConfigurableMetricExporterProvider.TestMetricExporter.class)
          .extracting("config")
          .isSameAs(config);
      assertThat(spiHelper.getListeners())
          .satisfiesExactlyInAnyOrder(
              listener ->
                  assertThat(listener).isInstanceOf(TestConfigurableMetricExporterProvider.class),
              listener -> assertThat(listener).isInstanceOf(OtlpMetricExporterProvider.class));
    }
  }

  @Test
  void configureExporter_emptyClassLoader() {
    assertThat(
            MetricExporterConfiguration.configureExporter(
                "testExporter",
                MetricExporterConfiguration.metricExporterSpiManager(
                    DefaultConfigProperties.createFromMap(Collections.emptyMap()),
                    SpiHelper.create(new URLClassLoader(new URL[] {}, null)))))
        .isNull();
  }

  @Test
  void configureExporter_exporterNotFound() {
    assertThat(
            MetricExporterConfiguration.configureExporter(
                "catExporter",
                MetricExporterConfiguration.metricExporterSpiManager(
                    DefaultConfigProperties.createFromMap(Collections.emptyMap()), spiHelper)))
        .isNull();
  }

  @Test
  void configureMetricReaders_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            ImmutableMap.of("otel.metrics.exporter", "otlp,none"));
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                MeterProviderConfiguration.configureMetricReaders(
                    config, spiHelper, (a, unused) -> a, (a, unused) -> a, closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.metrics.exporter contains none along with other exporters");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
    assertThat(spiHelper.getListeners()).isEmpty();
  }

  @Test
  void configureMetricReaders_defaultExporter() {
    ConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());
    List<Closeable> closeables = new ArrayList<>();

    List<MetricReader> metricReaders =
        MeterProviderConfiguration.configureMetricReaders(
            config,
            spiHelper,
            (a, unused) -> a,
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
        DefaultConfigProperties.createFromMap(
            ImmutableMap.of("otel.metrics.exporter", "otlp,logging"));
    List<Closeable> closeables = new ArrayList<>();

    List<MetricReader> metricReaders =
        MeterProviderConfiguration.configureMetricReaders(
            config,
            spiHelper,
            (a, unused) -> a,
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
