/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.stream.Collectors.toMap;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

final class SpanExporterConfiguration {

  private static final String EXPORTER_NONE = "none";
  private static final Map<String, String> EXPORTER_ARTIFACT_ID_BY_NAME;

  static {
    EXPORTER_ARTIFACT_ID_BY_NAME = new HashMap<>();
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging", "opentelemetry-exporter-logging");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging-otlp", "opentelemetry-exporter-logging-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("otlp", "opentelemetry-exporter-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("zipkin", "opentelemetry-exporter-zipkin");
  }

  // Visible for testing
  static Map<String, SpanExporter> configureSpanExporters(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      MeterProvider meterProvider,
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer) {
    Set<String> exporterNames = DefaultConfigProperties.getSet(config, "otel.traces.exporter");
    if (exporterNames.contains(EXPORTER_NONE)) {
      if (exporterNames.size() > 1) {
        throw new ConfigurationException(
            "otel.traces.exporter contains " + EXPORTER_NONE + " along with other exporters");
      }
      SpanExporter noop = SpanExporter.composite();
      SpanExporter customized = spanExporterCustomizer.apply(noop, config);
      if (customized == noop) {
        return Collections.emptyMap();
      }
      return Collections.singletonMap(EXPORTER_NONE, customized);
    }

    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton("otlp");
    }

    NamedSpiManager<SpanExporter> spiExportersManager =
        spanExporterSpiManager(config, serviceClassLoader);

    return exporterNames.stream()
        .collect(
            toMap(
                Function.identity(),
                exporterName ->
                    spanExporterCustomizer.apply(
                        configureExporter(exporterName, config, spiExportersManager, meterProvider),
                        config)));
  }

  // Visible for testing
  static NamedSpiManager<SpanExporter> spanExporterSpiManager(
      ConfigProperties config, ClassLoader serviceClassLoader) {
    return SpiUtil.loadConfigurable(
        ConfigurableSpanExporterProvider.class,
        ConfigurableSpanExporterProvider::getName,
        ConfigurableSpanExporterProvider::createExporter,
        config,
        serviceClassLoader);
  }

  // Visible for testing
  static SpanExporter configureExporter(
      String name,
      ConfigProperties config,
      NamedSpiManager<SpanExporter> spiExportersManager,
      MeterProvider meterProvider) {
    switch (name) {
      case "jaeger":
        return configureJaeger(config, meterProvider);
      default:
        SpanExporter spiExporter = spiExportersManager.getByName(name);
        if (spiExporter == null) {
          String artifactId = EXPORTER_ARTIFACT_ID_BY_NAME.get(name);
          if (artifactId != null) {
            throw new ConfigurationException(
                "otel.traces.exporter set to \""
                    + name
                    + "\" but "
                    + artifactId
                    + " not found on classpath. Make sure to add it as a dependency.");
          }
          throw new ConfigurationException("Unrecognized value for otel.traces.exporter: " + name);
        }
        return spiExporter;
    }
  }

  private static SpanExporter configureJaeger(
      ConfigProperties config, MeterProvider meterProvider) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter",
        "Jaeger gRPC Exporter",
        "opentelemetry-exporter-jaeger");
    JaegerGrpcSpanExporterBuilder builder = JaegerGrpcSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.jaeger.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    Duration timeout = config.getDuration("otel.exporter.jaeger.timeout");
    if (timeout != null) {
      builder.setTimeout(timeout);
    }

    builder.setMeterProvider(meterProvider);

    return builder.build();
  }

  private SpanExporterConfiguration() {}
}
