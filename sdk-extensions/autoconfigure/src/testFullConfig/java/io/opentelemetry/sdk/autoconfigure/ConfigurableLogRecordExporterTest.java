/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.provider.TestConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ConfigurableLogRecordExporterTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configureLogRecordExporters_spiExporter() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.logs.exporter", "testExporter"));
    List<Closeable> closeables = new ArrayList<>();

    Map<String, LogRecordExporter> exportersByName =
        LogRecordExporterConfiguration.configureLogRecordExporters(
            config,
            LogRecordExporterConfiguration.class.getClassLoader(),
            (a, unused) -> a,
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(exportersByName)
        .hasSize(1)
        .containsKey("testExporter")
        .extracting(map -> map.get("testExporter"))
        .isInstanceOf(TestConfigurableLogRecordExporterProvider.TestLogRecordExporter.class)
        .extracting("config")
        .isSameAs(config);
    assertThat(closeables)
        .hasExactlyElementsOfTypes(
            TestConfigurableLogRecordExporterProvider.TestLogRecordExporter.class);
  }

  @Test
  void configureLogRecordExporters_emptyClassLoader() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.logs.exporter", "testExporter"));
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureLogRecordExporters(
                    config, new URLClassLoader(new URL[0], null), (a, unused) -> a, closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureExporter_NotFound() {
    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureExporter(
                    "catExporter", NamedSpiManager.createEmpty()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }
}
