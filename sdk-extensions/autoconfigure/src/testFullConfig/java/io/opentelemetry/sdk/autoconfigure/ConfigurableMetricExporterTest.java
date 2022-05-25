/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class ConfigurableMetricExporterTest {

  @Test
  void configuration() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureSpiExporter(
            "testExporter", config, MetricExporterConfiguration.class.getClassLoader());

    assertThat(metricExporter)
        .isInstanceOf(TestConfigurableMetricExporterProvider.TestMetricExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void emptyClassLoader() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "testExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    new URLClassLoader(new URL[0], null),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
  }

  @Test
  void exporterNotFound() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "catExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    MetricExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }

  @Test
  void configureMetricExporters_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.metrics.exporter", "otlp,none"));

    assertThatThrownBy(
            () ->
                MeterProviderConfiguration.configureMeterProvider(
                    SdkMeterProvider.builder(),
                    config,
                    MetricExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.metrics.exporter contains none along with other exporters");
  }

  @Test
  void defaultExporter() {
    ConfigProperties config = DefaultConfigProperties.createForTest(Collections.emptyMap());

    assertThat(
            MeterProviderConfiguration.configureMetricReaders(
                config,
                MeterProviderConfiguration.class.getClassLoader(),
                (metricExporter, unused) -> metricExporter))
        .hasSize(1)
        .first()
        .isInstanceOf(PeriodicMetricReader.class)
        .extracting("exporter")
        .isInstanceOf(OtlpGrpcMetricExporter.class)
        .satisfies(
            metricExporter ->
                ((OtlpGrpcMetricExporter) metricExporter).shutdown().join(10, TimeUnit.SECONDS));
  }

  @Test
  void configureMultipleMetricExporters() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.metrics.exporter", "otlp,prometheus"));

    assertThat(
            MeterProviderConfiguration.configureMetricReaders(
                config,
                MeterProviderConfiguration.class.getClassLoader(),
                (metricExporter, unused) -> metricExporter))
        .hasSize(2)
        .hasAtLeastOneElementOfType(PeriodicMetricReader.class)
        .hasAtLeastOneElementOfType(PrometheusHttpServer.class)
        .allSatisfy(metricReader -> metricReader.shutdown().join(10, TimeUnit.SECONDS));
  }
}
