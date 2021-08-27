/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class NotOnClasspathTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createForTest(Collections.emptyMap());

  @Test
  void otlpGrpcSpans() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter("otlp", EMPTY, Collections.emptyMap()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "OTLP gRPC Trace Exporter enabled but opentelemetry-exporter-otlp not found on "
                + "classpath");
  }

  @Test
  void otlpHttpSpans() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.experimental.exporter.otlp.protocol", "http/protobuf"));
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter("otlp", config, Collections.emptyMap()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "OTLP HTTP Trace Exporter enabled but opentelemetry-exporter-otlp-http-trace not found on "
                + "classpath");
  }

  @Test
  void jaeger() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "jaeger", EMPTY, Collections.emptyMap()))
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
                    "zipkin", EMPTY, Collections.emptyMap()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "Zipkin Exporter enabled but opentelemetry-exporter-zipkin not found on classpath");
  }

  @Test
  void logging() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "logging", EMPTY, Collections.emptyMap()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "Logging Trace Exporter enabled but opentelemetry-exporter-logging not found on "
                + "classpath");
  }

  @Test
  void logging_metrics() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "logging", EMPTY, SdkMeterProvider.builder().build()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(
            "Logging Metrics Exporter enabled but opentelemetry-exporter-logging not found on "
                + "classpath");
  }

  @Test
  void otlpGrpcMetrics() {
    assertThatCode(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "otlp", EMPTY, SdkMeterProvider.builder().build()))
        .doesNotThrowAnyException();
  }

  @Test
  void otlpHttpMetrics() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.experimental.exporter.otlp.protocol", "http/protobuf"));
    assertThatCode(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "otlp", config, SdkMeterProvider.builder().build()))
        .doesNotThrowAnyException();
  }

  @Test
  void prometheus() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "prometheus", EMPTY, SdkMeterProvider.builder().build()))
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
                        Collections.singletonMap("otel.propagators", "b3"))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.propagators: b3");
  }
}
