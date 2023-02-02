/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.provider.TestConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ConfigurableSpanExporterTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configureSpanExporters_spiExporter() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.traces.exporter", "testExporter"));
    List<Closeable> closeables = new ArrayList<>();

    Map<String, SpanExporter> exportersByName =
        SpanExporterConfiguration.configureSpanExporters(
            config, SpanExporterConfiguration.class.getClassLoader(), (a, unused) -> a, closeables);
    cleanup.addCloseables(closeables);

    assertThat(exportersByName)
        .hasSize(1)
        .containsKey("testExporter")
        .extracting(map -> map.get("testExporter"))
        .isInstanceOf(TestConfigurableSpanExporterProvider.TestSpanExporter.class)
        .extracting("config")
        .isSameAs(config);
    assertThat(closeables)
        .hasExactlyElementsOfTypes(TestConfigurableSpanExporterProvider.TestSpanExporter.class);
  }

  @Test
  void configureSpanExporters_emptyClassLoader() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("test.option", "true", "otel.traces.exporter", "testExporter"));
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureSpanExporters(
                    config,
                    new URLClassLoader(new URL[0], null),
                    (a, unused) -> a,
                    new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureSpanExporters_duplicates() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.traces.exporter", "otlp,otlp,logging"));
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureSpanExporters(
                    config,
                    SpanExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a,
                    closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.traces.exporter contains duplicates: [otlp]");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureSpanExporters_multipleWithNone() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("otel.traces.exporter", "otlp,none"));
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureSpanExporters(
                    config,
                    SpanExporterConfiguration.class.getClassLoader(),
                    (a, unused) -> a,
                    closeables))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("otel.traces.exporter contains none along with other exporters");
    cleanup.addCloseables(closeables);
    assertThat(closeables).isEmpty();
  }

  @Test
  void configureExporter_NotFound() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "catExporter", NamedSpiManager.createEmpty()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }

  @Test
  void configureSpanProcessors_simpleSpanProcessor() {
    String exporterName = "logging";
    List<Closeable> closeables = new ArrayList<>();

    List<SpanProcessor> spanProcessors =
        TracerProviderConfiguration.configureSpanProcessors(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.traces.exporter", exporterName)),
            ImmutableMap.of(exporterName, LoggingSpanExporter.create()),
            MeterProvider.noop(),
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(spanProcessors).hasExactlyElementsOfTypes(SimpleSpanProcessor.class);
    assertThat(closeables).hasExactlyElementsOfTypes(SimpleSpanProcessor.class);
  }

  @Test
  void configureSpanProcessors_batchSpanProcessor() {
    String exporterName = "zipkin";
    List<Closeable> closeables = new ArrayList<>();

    List<SpanProcessor> spanProcessors =
        TracerProviderConfiguration.configureSpanProcessors(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.traces.exporter", exporterName)),
            ImmutableMap.of(exporterName, ZipkinSpanExporter.builder().build()),
            MeterProvider.noop(),
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(spanProcessors).hasExactlyElementsOfTypes(BatchSpanProcessor.class);
    assertThat(closeables).hasExactlyElementsOfTypes(BatchSpanProcessor.class);
  }

  @Test
  void configureSpanProcessors_multipleExporters() {
    List<Closeable> closeables = new ArrayList<>();

    List<SpanProcessor> spanProcessors =
        TracerProviderConfiguration.configureSpanProcessors(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.traces.exporter", "otlp,zipkin")),
            ImmutableMap.of(
                "otlp",
                OtlpGrpcSpanExporter.builder().build(),
                "zipkin",
                ZipkinSpanExporter.builder().build()),
            MeterProvider.noop(),
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(spanProcessors)
        .hasExactlyElementsOfTypes(BatchSpanProcessor.class)
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
    assertThat(closeables).hasExactlyElementsOfTypes(BatchSpanProcessor.class);
  }

  @Test
  void configureSpanProcessors_multipleExportersWithLogging() {
    List<Closeable> closeables = new ArrayList<>();

    List<SpanProcessor> spanProcessors =
        TracerProviderConfiguration.configureSpanProcessors(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.traces.exporter", "logging,zipkin")),
            ImmutableMap.of(
                "logging",
                LoggingSpanExporter.create(),
                "zipkin",
                ZipkinSpanExporter.builder().build()),
            MeterProvider.noop(),
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(spanProcessors)
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleSpanProcessor.class)
        .hasAtLeastOneElementOfType(BatchSpanProcessor.class);
    assertThat(closeables)
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleSpanProcessor.class)
        .hasAtLeastOneElementOfType(BatchSpanProcessor.class);
  }
}
