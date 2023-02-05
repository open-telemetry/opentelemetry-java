/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

final class SpanExporterConfiguration {

  private static final String EXPORTER_NONE = "none";
  private static final Map<String, String> EXPORTER_ARTIFACT_ID_BY_NAME;

  static {
    EXPORTER_ARTIFACT_ID_BY_NAME = new HashMap<>();
    EXPORTER_ARTIFACT_ID_BY_NAME.put("jaeger", "opentelemetry-exporter-jaeger");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging", "opentelemetry-exporter-logging");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging-otlp", "opentelemetry-exporter-logging-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("otlp", "opentelemetry-exporter-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("zipkin", "opentelemetry-exporter-zipkin");
  }

  // Visible for testing
  static Map<String, SpanExporter> configureSpanExporters(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer,
      List<Closeable> closeables) {
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
      closeables.add(customized);
      return Collections.singletonMap(EXPORTER_NONE, customized);
    }

    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton("otlp");
    }

    NamedSpiManager<SpanExporter> spiExportersManager =
        spanExporterSpiManager(config, serviceClassLoader);

    Map<String, SpanExporter> map = new HashMap<>();
    for (String exporterName : exporterNames) {
      SpanExporter spanExporter = configureExporter(exporterName, spiExportersManager);
      closeables.add(spanExporter);
      SpanExporter customizedSpanExporter = spanExporterCustomizer.apply(spanExporter, config);
      if (customizedSpanExporter != spanExporter) {
        closeables.add(customizedSpanExporter);
      }
      map.put(exporterName, customizedSpanExporter);
    }
    return Collections.unmodifiableMap(map);
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
      String name, NamedSpiManager<SpanExporter> spiExportersManager) {
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

  private SpanExporterConfiguration() {}
}
