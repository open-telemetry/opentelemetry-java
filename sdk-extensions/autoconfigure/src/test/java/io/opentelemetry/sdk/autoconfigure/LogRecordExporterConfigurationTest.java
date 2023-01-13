/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.LogRecordExporterConfiguration.configureExporter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LogRecordExporterConfigurationTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

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
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    DefaultConfigProperties.createForTest(
                        ImmutableMap.of("otel.logs.exporter", "otlp,otlp")),
                    LogRecordExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a,
                    closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.logs.exporter contains duplicates: [otlp]");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureLogRecordExporters_unrecognized() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    DefaultConfigProperties.createForTest(
                        ImmutableMap.of("otel.logs.exporter", "foo")),
                    LogRecordExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a,
                    new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.logs.exporter: foo");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureLogRecordExporters_multipleWithNone() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    DefaultConfigProperties.createForTest(
                        ImmutableMap.of("otel.logs.exporter", "otlp,none")),
                    LogRecordExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a,
                    closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.logs.exporter contains none along with other exporters");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }
}
