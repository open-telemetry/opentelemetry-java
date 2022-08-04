/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigurableLogExporterTest {

  @Test
  void configureLogExporters_spiExporter() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.logs.exporter", "testExporter"));
    Map<String, LogExporter> exportersByName =
        LogExporterConfiguration.configureLogExporters(
            config,
            LogExporterConfiguration.class.getClassLoader(),
            MeterProvider.noop(),
            (a, unused) -> a);

    assertThat(exportersByName)
        .hasSize(1)
        .containsKey("testExporter")
        .extracting(map -> map.get("testExporter"))
        .isInstanceOf(TestConfigurableLogExporterProvider.TestLogExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void configureLogExporters_emptyClassLoader() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.logs.exporter", "testExporter"));
    assertThatThrownBy(
            () ->
                LogExporterConfiguration.configureLogExporters(
                    config,
                    new URLClassLoader(new URL[0], null),
                    MeterProvider.noop(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
  }

  @Test
  void configureExporter_NotFound() {
    assertThatThrownBy(
            () ->
                LogExporterConfiguration.configureExporter(
                    "catExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    NamedSpiManager.createEmpty(),
                    MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }
}
