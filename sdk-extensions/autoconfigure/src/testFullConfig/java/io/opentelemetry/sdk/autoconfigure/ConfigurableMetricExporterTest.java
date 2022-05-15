/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
                    SdkMeterProvider.builder(),
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
                    SdkMeterProvider.builder(),
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

    SdkMeterProviderBuilder builder = Mockito.mock(SdkMeterProviderBuilder.class);
    List<MetricReader> metricReaders = new ArrayList<>();
    Mockito.when(builder.registerMetricReader(Mockito.any()))
        .thenAnswer(
            invocation -> {
              metricReaders.add(invocation.getArgument(0));
              return builder;
            });
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        config,
        MetricExporterConfiguration.class.getClassLoader(),
        (metricExporter, unused) -> metricExporter);

    Mockito.verify(builder)
        .registerMetricReader(
            Mockito.argThat(argument -> argument instanceof PeriodicMetricReader));
    metricReaders.forEach(metricReader -> metricReader.shutdown().join(10, TimeUnit.SECONDS));
  }

  @Test
  void configureMultipleMetricExporters() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.metrics.exporter", "otlp,prometheus"));
    SdkMeterProviderBuilder builder = Mockito.mock(SdkMeterProviderBuilder.class);
    List<MetricReader> metricReaders = new ArrayList<>();
    Mockito.when(builder.registerMetricReader(Mockito.any()))
        .thenAnswer(
            invocation -> {
              metricReaders.add(invocation.getArgument(0));
              return builder;
            });
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        config,
        MetricExporterConfiguration.class.getClassLoader(),
        (metricExporter, unused) -> metricExporter);

    Mockito.verify(builder)
        .registerMetricReader(
            Mockito.argThat(argument -> argument instanceof PeriodicMetricReader));
    Mockito.verify(builder)
        .registerMetricReader(
            Mockito.argThat(argument -> argument instanceof PrometheusHttpServer));
    metricReaders.forEach(metricReader -> metricReader.shutdown().join(10, TimeUnit.SECONDS));
  }
}
