/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ConfigurableMetricExporterTest {

  @Test
  void configuration() {
    ConfigProperties config =
        ConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureSpiExporter("testExporter", config);

    assertThat(metricExporter)
        .isInstanceOf(TestConfigurableMetricExporterProvider.TestMetricExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void exporterNotFound() {
    SdkMeterProvider provider = SdkMeterProvider.builder().build();
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "catExporter",
                    ConfigProperties.createForTest(Collections.emptyMap()),
                    provider))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }
}
