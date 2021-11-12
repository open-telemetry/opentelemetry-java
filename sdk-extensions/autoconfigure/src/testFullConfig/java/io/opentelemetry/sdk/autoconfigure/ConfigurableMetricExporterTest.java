/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ConfigurableMetricExporterTest {

  @Test
  void configuration() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureSpiExporter(
            "testExporter", config, MetricExporterConfiguration.class.getClassLoader());

    assertThat(metricExporter)
        .isInstanceOf(TestConfigurableMetricExporterProvider.TestMetricExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void emptyClassLoader() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "testExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    new URLClassLoader(new URL[0], null),
                    SdkMeterProvider.builder()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
  }

  @Test
  void exporterNotFound() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "catExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    MetricExporterConfiguration.class.getClassLoader(),
                    SdkMeterProvider.builder()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }
}
