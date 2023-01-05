/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.LogRecordExporterConfiguration.configureExporter;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LogRecordExporterConfigurationTest {

  @Test
  void configureExporter_KnownSpiExportersNotOnClasspath() {
    NamedSpiManager<LogRecordExporter> spiExportersManager =
        LogRecordExporterConfiguration.logRecordExporterSpiManager(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            LogRecordExporterConfigurationTest.class.getClassLoader());

    assertThatThrownBy(() -> configureExporter("logging", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.logs.exporter set to \"logging\" but opentelemetry-exporter-logging"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.logs.exporter set to \"logging-otlp\" but opentelemetry-exporter-logging-otlp"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("otlp", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.logs.exporter set to \"otlp\" but opentelemetry-exporter-otlp-logs"
                + " not found on classpath. Make sure to add it as a dependency.");

    // Unrecognized exporter
    assertThatThrownBy(() -> configureExporter("foo", spiExportersManager))
        .hasMessage("Unrecognized value for otel.logs.exporter: foo");
  }

  @Test
  void configureLogRecordExporters_duplicates() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.logs.exporter", "otlp,otlp"));

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    config,
                    LogRecordExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.logs.exporter contains duplicates: [otlp]");
  }

  @Test
  void configureLogRecordExporters_unrecognized() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.logs.exporter", "foo"));

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    config,
                    LogRecordExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.logs.exporter: foo");
  }

  @Test
  void configureLogRecordExporters_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.logs.exporter", "otlp,none"));

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    config,
                    LogRecordExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.logs.exporter contains none along with other exporters");
  }
}
