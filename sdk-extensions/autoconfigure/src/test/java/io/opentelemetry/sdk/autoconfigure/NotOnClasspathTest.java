/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class NotOnClasspathTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createForTest(Collections.emptyMap());
  private static final NamedSpiManager<SpanExporter> SPAN_EXPORTER_SPI_MANAGER =
      SpanExporterConfiguration.spanExporterSpiManager(
          EMPTY, NotOnClasspathTest.class.getClassLoader());
  private static final NamedSpiManager<MetricExporter> METRIC_EXPORTER_SPI_MANAGER =
      MetricExporterConfiguration.metricExporterSpiManager(
          EMPTY, NotOnClasspathTest.class.getClassLoader());
  private static final NamedSpiManager<LogRecordExporter> LOG_RECORD_EXPORTER_SPI_MANAGER =
      LogRecordExporterConfiguration.logRecordExporterSpiManager(
          EMPTY, NotOnClasspathTest.class.getClassLoader());

  @Test
  void otlpSpans() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", EMPTY, SPAN_EXPORTER_SPI_MANAGER, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.traces.exporter set to \"otlp\" but opentelemetry-exporter-otlp not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void jaeger() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "jaeger", EMPTY, SPAN_EXPORTER_SPI_MANAGER, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "Jaeger gRPC Exporter enabled but opentelemetry-exporter-jaeger not found on "
                + "classpath");
  }

  @Test
  void zipkin() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "zipkin", EMPTY, SPAN_EXPORTER_SPI_MANAGER, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.traces.exporter set to \"zipkin\" but opentelemetry-exporter-zipkin not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void loggingSpans() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "logging", EMPTY, SPAN_EXPORTER_SPI_MANAGER, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.traces.exporter set to \"logging\" but opentelemetry-exporter-logging not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void loggingSpansOtlp() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "logging-otlp", EMPTY, SPAN_EXPORTER_SPI_MANAGER, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.traces.exporter set to \"logging-otlp\" but opentelemetry-exporter-logging-otlp not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void loggingMetrics() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "logging", METRIC_EXPORTER_SPI_MANAGER))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.metrics.exporter set to \"logging\" but opentelemetry-exporter-logging not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void loggingMetricsOtlp() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "logging-otlp", METRIC_EXPORTER_SPI_MANAGER))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.metrics.exporter set to \"logging-otlp\" but opentelemetry-exporter-logging-otlp not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void loggingLogs() {
    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureExporter(
                    "logging", LOG_RECORD_EXPORTER_SPI_MANAGER))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.logs.exporter set to \"logging\" but opentelemetry-exporter-logging not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void loggingLogsOtlp() {
    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureExporter(
                    "logging-otlp", LOG_RECORD_EXPORTER_SPI_MANAGER))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.logs.exporter set to \"logging-otlp\" but opentelemetry-exporter-logging-otlp not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void otlpMetrics() {
    assertThatCode(
            () ->
                MetricExporterConfiguration.configureExporter("otlp", METRIC_EXPORTER_SPI_MANAGER))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.metrics.exporter set to \"otlp\" but opentelemetry-exporter-otlp not found on classpath."
                + " Make sure to add it as a dependency.");
  }

  @Test
  void prometheus() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureReader(
                    "prometheus",
                    EMPTY,
                    NotOnClasspathTest.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "Prometheus Metrics Server enabled but opentelemetry-exporter-prometheus not found on "
                + "classpath");
  }

  @Test
  void b3propagator() {
    assertThatThrownBy(
            () ->
                PropagatorConfiguration.configurePropagators(
                    DefaultConfigProperties.createForTest(
                        Collections.singletonMap("otel.propagators", "b3")),
                    PropagatorConfiguration.class.getClassLoader(),
                    (a, config) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.propagators: b3");
  }

  @Test
  void otlpGrpcLogs() {
    assertThatCode(
            () ->
                LogRecordExporterConfiguration.configureExporter(
                    "otlp", LOG_RECORD_EXPORTER_SPI_MANAGER))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "otel.logs.exporter set to \"otlp\" but opentelemetry-exporter-otlp not found on classpath."
                + " Make sure to add it as a dependency.");
  }
}
