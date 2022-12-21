/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.SpanExporterConfiguration.configureExporter;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanExporterConfigurationTest {

  @Test
  void configureExporter_KnownSpiExportersNotOnClasspath() {
    NamedSpiManager<SpanExporter> spiExportersManager =
        SpanExporterConfiguration.spanExporterSpiManager(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            SpanExporterConfigurationTest.class.getClassLoader());

    assertThatThrownBy(() -> configureExporter("jaeger", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.traces.exporter set to \"jaeger\" but opentelemetry-exporter-jaeger"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("logging", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.traces.exporter set to \"logging\" but opentelemetry-exporter-logging"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.traces.exporter set to \"logging-otlp\" but opentelemetry-exporter-logging-otlp"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("otlp", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.traces.exporter set to \"otlp\" but opentelemetry-exporter-otlp"
                + " not found on classpath. Make sure to add it as a dependency.");
    assertThatThrownBy(() -> configureExporter("zipkin", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "otel.traces.exporter set to \"zipkin\" but opentelemetry-exporter-zipkin"
                + " not found on classpath. Make sure to add it as a dependency.");

    // Unrecognized exporter
    assertThatThrownBy(() -> configureExporter("foo", spiExportersManager))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unrecognized value for otel.traces.exporter: foo");
  }
}
