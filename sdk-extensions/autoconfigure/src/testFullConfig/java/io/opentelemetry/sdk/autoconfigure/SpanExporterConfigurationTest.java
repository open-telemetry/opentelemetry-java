/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanExporterConfigurationTest {

  @Test
  void configureOtlpSpansUnsupportedProtocol() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.exporter.otlp.protocol", "foo"));
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp",
                    SpanExporterConfiguration.spanExporterSpiManager(
                        config, SpanExporterConfigurationTest.class.getClassLoader())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP traces protocol: foo");
  }

  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureOtlpTimeout() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.exporter.otlp.timeout", "10"));
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            SpanExporterConfiguration.spanExporterSpiManager(
                config, SpanExporterConfigurationTest.class.getClassLoader()));
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OtlpGrpcSpanExporter.class,
              otlp ->
                  assertThat(otlp).extracting("delegate.client.callTimeoutMillis").isEqualTo(10));
    } finally {
      exporter.shutdown();
    }
  }

  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureJaegerTimeout() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.exporter.jaeger.timeout", "10"));
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "jaeger",
            SpanExporterConfiguration.spanExporterSpiManager(
                config, SpanExporterConfigurationTest.class.getClassLoader()));
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              JaegerGrpcSpanExporter.class,
              jaeger ->
                  assertThat(jaeger).extracting("delegate.client.callTimeoutMillis").isEqualTo(10));
    } finally {
      exporter.shutdown();
    }
  }

  // Timeout difficult to test using real exports so just check that things don't blow up.
  @Test
  void configureZipkinTimeout() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.exporter.zipkin.timeout", "5s"));
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "zipkin",
            SpanExporterConfiguration.spanExporterSpiManager(
                config, SpanExporterConfigurationTest.class.getClassLoader()));
    try {
      assertThat(exporter).isNotNull();
    } finally {
      exporter.shutdown();
    }
  }
}
