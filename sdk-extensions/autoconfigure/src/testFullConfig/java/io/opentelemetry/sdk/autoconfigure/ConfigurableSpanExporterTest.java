/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

public class ConfigurableSpanExporterTest {

  @Test
  void configureSpanExporters_spiExporter() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.traces.exporter", "testExporter"));
    Map<String, SpanExporter> exportersByName =
        SpanExporterConfiguration.configureSpanExporters(config);

    assertThat(exportersByName)
        .hasSize(1)
        .containsKey("testExporter")
        .extracting(map -> map.get("testExporter"))
        .isInstanceOf(TestConfigurableSpanExporterProvider.TestSpanExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void configureSpanExporters_duplicates() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.traces.exporter", "otlp,otlp,logging"));

    assertThatThrownBy(() -> SpanExporterConfiguration.configureSpanExporters(config))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.traces.exporter contains duplicates: [otlp]");
  }

  @Test
  void configureSpanExporters_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.traces.exporter", "otlp,none"));

    assertThatThrownBy(() -> SpanExporterConfiguration.configureSpanExporters(config))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.traces.exporter contains none along with other exporters");
  }

  @Test
  void exporterNotFound() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "catExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    Collections.emptyMap()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }

  @Test
  void configureSpanProcessors_simpleSpanProcessor() {
    String exporterName = "logging";
    Map<String, String> propMap = Collections.singletonMap("otel.traces.exporter", exporterName);
    SpanExporter exporter = new LoggingSpanExporter();
    ConfigProperties properties = DefaultConfigProperties.createForTest(propMap);

    assertThat(
            TracerProviderConfiguration.configureSpanProcessors(
                properties, ImmutableMap.of(exporterName, exporter)))
        .hasSize(1)
        .first()
        .isInstanceOf(SimpleSpanProcessor.class);
  }

  @Test
  void configureSpanProcessors_batchSpanProcessor() {
    String exporterName = "zipkin";
    Map<String, String> propMap = Collections.singletonMap("otel.traces.exporter", exporterName);
    SpanExporter exporter = ZipkinSpanExporter.builder().build();
    ConfigProperties properties = DefaultConfigProperties.createForTest(propMap);

    assertThat(
            TracerProviderConfiguration.configureSpanProcessors(
                properties, ImmutableMap.of(exporterName, exporter)))
        .hasSize(1)
        .first()
        .isInstanceOf(BatchSpanProcessor.class);
  }

  @Test
  void configureSpanProcessors_multipleExporters() {
    SpanExporter otlpExporter = OtlpGrpcSpanExporter.builder().build();
    SpanExporter zipkinExporter = ZipkinSpanExporter.builder().build();
    ConfigProperties properties =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.traces.exporter", "otlp,zipkin"));

    assertThat(
            TracerProviderConfiguration.configureSpanProcessors(
                properties, ImmutableMap.of("otlp", otlpExporter, "zipkin", zipkinExporter)))
        .hasSize(1)
        .hasAtLeastOneElementOfType(BatchSpanProcessor.class)
        .first()
        .extracting("worker")
        .extracting("spanExporter")
        .asInstanceOf(InstanceOfAssertFactories.type(SpanExporter.class))
        .satisfies(
            spanExporter -> {
              assertThat(spanExporter)
                  .extracting(spanExporter1 -> spanExporter1.getClass().getSimpleName())
                  .isEqualTo("MultiSpanExporter");
              assertThat(spanExporter)
                  .extracting("spanExporters")
                  .asInstanceOf(InstanceOfAssertFactories.type(SpanExporter[].class))
                  .satisfies(
                      spanExporters -> {
                        assertThat(spanExporters.length).isEqualTo(2);
                        assertThat(spanExporters)
                            .hasAtLeastOneElementOfType(ZipkinSpanExporter.class)
                            .hasAtLeastOneElementOfType(OtlpGrpcSpanExporter.class);
                      });
            });
  }

  @Test
  void configureSpanProcessors_multipleExportersWithLogging() {
    SpanExporter loggingExporter = new LoggingSpanExporter();
    SpanExporter zipkinExporter = ZipkinSpanExporter.builder().build();
    ConfigProperties properties =
        DefaultConfigProperties.createForTest(
            Collections.singletonMap("otel.traces.exporter", "logging,zipkin"));

    assertThat(
            TracerProviderConfiguration.configureSpanProcessors(
                properties, ImmutableMap.of("logging", loggingExporter, "zipkin", zipkinExporter)))
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleSpanProcessor.class)
        .hasAtLeastOneElementOfType(BatchSpanProcessor.class);
  }
}
