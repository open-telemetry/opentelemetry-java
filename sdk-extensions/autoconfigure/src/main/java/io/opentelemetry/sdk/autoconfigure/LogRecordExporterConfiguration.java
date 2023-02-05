/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

class LogRecordExporterConfiguration {

  private static final String EXPORTER_NONE = "none";
  private static final Map<String, String> EXPORTER_ARTIFACT_ID_BY_NAME;

  static {
    EXPORTER_ARTIFACT_ID_BY_NAME = new HashMap<>();
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging", "opentelemetry-exporter-logging");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging-otlp", "opentelemetry-exporter-logging-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("otlp", "opentelemetry-exporter-otlp-logs");
  }

  // Visible for test
  static Map<String, LogRecordExporter> configureLogRecordExporters(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
          logRecordExporterCustomizer,
      List<Closeable> closeables) {
    Set<String> exporterNames = DefaultConfigProperties.getSet(config, "otel.logs.exporter");

    // Default to no exporter
    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton(EXPORTER_NONE);
    }

    if (exporterNames.contains(EXPORTER_NONE)) {
      if (exporterNames.size() > 1) {
        throw new ConfigurationException(
            "otel.logs.exporter contains " + EXPORTER_NONE + " along with other exporters");
      }
      return Collections.emptyMap();
    }

    NamedSpiManager<LogRecordExporter> spiExportersManager =
        logRecordExporterSpiManager(config, serviceClassLoader);

    Map<String, LogRecordExporter> exportersByName = new HashMap<>();
    for (String name : exporterNames) {
      LogRecordExporter logRecordExporter = configureExporter(name, spiExportersManager);
      closeables.add(logRecordExporter);
      LogRecordExporter customizedLogRecordExporter =
          logRecordExporterCustomizer.apply(logRecordExporter, config);
      if (customizedLogRecordExporter != logRecordExporter) {
        closeables.add(customizedLogRecordExporter);
      }
      exportersByName.put(name, customizedLogRecordExporter);
    }

    return Collections.unmodifiableMap(exportersByName);
  }

  // Visible for testing
  static NamedSpiManager<LogRecordExporter> logRecordExporterSpiManager(
      ConfigProperties config, ClassLoader serviceClassLoader) {
    return SpiUtil.loadConfigurable(
        ConfigurableLogRecordExporterProvider.class,
        ConfigurableLogRecordExporterProvider::getName,
        ConfigurableLogRecordExporterProvider::createExporter,
        config,
        serviceClassLoader);
  }

  // Visible for testing
  static LogRecordExporter configureExporter(
      String name, NamedSpiManager<LogRecordExporter> spiExportersManager) {
    LogRecordExporter spiExporter = spiExportersManager.getByName(name);
    if (spiExporter == null) {
      String artifactId = EXPORTER_ARTIFACT_ID_BY_NAME.get(name);
      if (artifactId != null) {
        throw new ConfigurationException(
            "otel.logs.exporter set to \""
                + name
                + "\" but "
                + artifactId
                + " not found on classpath. Make sure to add it as a dependency.");
      }
      throw new ConfigurationException("Unrecognized value for otel.logs.exporter: " + name);
    }
    return spiExporter;
  }

  private LogRecordExporterConfiguration() {}
}
