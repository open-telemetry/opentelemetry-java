/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import org.junit.jupiter.api.Test;

public class MetricExporterConfigurationTest {

  @Test
  void configureOtlpMetricsUnsupportedProtocol() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "otlp",
                    MetricExporterConfiguration.metricExporterSpiManager(
                        DefaultConfigProperties.createForTest(
                            ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                        MetricExporterConfigurationTest.class.getClassLoader())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP metrics protocol: foo");
  }
}
