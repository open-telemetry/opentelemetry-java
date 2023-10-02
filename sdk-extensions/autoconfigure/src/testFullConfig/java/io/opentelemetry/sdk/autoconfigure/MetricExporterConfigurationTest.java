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
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MetricExporterConfigurationTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createFromMap(Collections.emptyMap());

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(MetricExporterConfigurationTest.class.getClassLoader());

  @Test
  void configureReader_PrometheusOnClasspath() {
    List<Closeable> closeables = new ArrayList<>();

    MetricReader reader =
        MetricExporterConfiguration.configureReader(
            "prometheus", EMPTY, spiHelper, (a, b) -> a, closeables);
    cleanup.addCloseables(closeables);

    assertThat(reader).isInstanceOf(PrometheusHttpServer.class);
    assertThat(closeables).hasSize(1);
  }

  @ParameterizedTest
  @MethodSource("knownExporters")
  void configureExporter_KnownSpiExportersOnClasspath(
      String exporterName, Class<? extends Closeable> expectedExporter) {
    NamedSpiManager<MetricExporter> spiExportersManager =
        MetricExporterConfiguration.metricExporterSpiManager(EMPTY, spiHelper);

    MetricExporter metricExporter =
        MetricExporterConfiguration.configureExporter(exporterName, spiExportersManager);
    cleanup.addCloseable(metricExporter);

    assertThat(metricExporter).isInstanceOf(expectedExporter);
  }

  private static Stream<Arguments> knownExporters() {
    return Stream.of(
        Arguments.of("logging", LoggingMetricExporter.class),
        Arguments.of("logging-otlp", OtlpJsonLoggingMetricExporter.class),
        Arguments.of("otlp", OtlpGrpcMetricExporter.class));
  }

  @ParameterizedTest
  @MethodSource("knownReaders")
  void configureMetricReader_KnownSpiExportersOnClasspath(
      String exporterName, Class<? extends Closeable> expectedExporter) {
    NamedSpiManager<MetricReader> spiMetricReadersManager =
        MetricExporterConfiguration.metricReadersSpiManager(EMPTY, spiHelper);

    MetricReader metricReader =
        MetricExporterConfiguration.configureMetricReader(exporterName, spiMetricReadersManager);
    cleanup.addCloseable(metricReader);

    assertThat(metricReader).isInstanceOf(expectedExporter);
  }

  private static Stream<Arguments> knownReaders() {
    return Stream.of(Arguments.of("prometheus", PrometheusHttpServer.class));
  }

  @Test
  void configureOtlpMetricsUnsupportedProtocol() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "otlp",
                    MetricExporterConfiguration.metricExporterSpiManager(
                        DefaultConfigProperties.createFromMap(
                            ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                        spiHelper)))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP metrics protocol: foo");
  }
}
