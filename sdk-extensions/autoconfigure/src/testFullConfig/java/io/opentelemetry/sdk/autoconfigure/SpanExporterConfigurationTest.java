/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanExporterConfigurationTest {

  @Test
  void configureOtlpSpansUnsupportedProtocol() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureOtlp(
                    DefaultConfigProperties.createForTest(
                        ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                    MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP traces protocol: foo");
  }

  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureOtlpTimeout() {
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.otlp.timeout", "10")),
            NamedSpiManager.createEmpty(),
            MeterProvider.noop());
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
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "jaeger",
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.jaeger.timeout", "10")),
            NamedSpiManager.createEmpty(),
            MeterProvider.noop());
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
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "zipkin",
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.zipkin.timeout", "5s")),
            NamedSpiManager.createEmpty(),
            MeterProvider.noop());
    try {
      assertThat(exporter).isNotNull();
    } finally {
      exporter.shutdown();
    }
  }
}
