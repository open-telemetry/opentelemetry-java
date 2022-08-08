/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import org.junit.jupiter.api.Test;

class LogExporterConfigurationTest {

  @Test
  void configureLogExporters_duplicates() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.logs.exporter", "otlp,otlp"));

    assertThatThrownBy(
            () ->
                LogExporterConfiguration.configureLogExporters(
                    config,
                    LogExporterConfiguration.class.getClassLoader(),
                    MeterProvider.noop(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.logs.exporter contains duplicates: [otlp]");
  }

  @Test
  void configureLogExporters_unrecognized() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.logs.exporter", "foo"));

    assertThatThrownBy(
            () ->
                LogExporterConfiguration.configureLogExporters(
                    config,
                    LogExporterConfiguration.class.getClassLoader(),
                    MeterProvider.noop(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.logs.exporter: foo");
  }

  @Test
  void configureLogExporters_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.logs.exporter", "otlp,none"));

    assertThatThrownBy(
            () ->
                LogExporterConfiguration.configureLogExporters(
                    config,
                    LogExporterConfiguration.class.getClassLoader(),
                    MeterProvider.noop(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.logs.exporter contains none along with other exporters");
  }

  @Test
  void configureOtlpLogs_unsupportedProtocol() {
    assertThatThrownBy(
            () ->
                LogExporterConfiguration.configureOtlpLogs(
                    DefaultConfigProperties.createForTest(
                        ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                    MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP logs protocol: foo");
  }
}
