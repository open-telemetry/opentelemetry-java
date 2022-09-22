/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.LogExporterConfiguration.configureLogExporters;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogLimitsBuilder;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

final class LoggerProviderConfiguration {

  static void configureLoggerProvider(
      SdkLoggerProviderBuilder loggerProviderBuilder,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      MeterProvider meterProvider,
      BiFunction<? super LogExporter, ConfigProperties, ? extends LogExporter>
          logExporterCustomizer) {

    loggerProviderBuilder.setLogLimits(() -> configureLogLimits(config));

    Map<String, LogExporter> exportersByName =
        configureLogExporters(config, serviceClassLoader, meterProvider, logExporterCustomizer);

    configureLogRecordProcessors(exportersByName, meterProvider)
        .forEach(loggerProviderBuilder::addLogRecordProcessor);
  }

  // Visible for testing
  static List<LogRecordProcessor> configureLogRecordProcessors(
      Map<String, LogExporter> exportersByName, MeterProvider meterProvider) {
    Map<String, LogExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<LogRecordProcessor> logRecordProcessors = new ArrayList<>();

    LogExporter exporter = exportersByNameCopy.remove("logging");
    if (exporter != null) {
      logRecordProcessors.add(SimpleLogRecordProcessor.create(exporter));
    }

    if (!exportersByNameCopy.isEmpty()) {
      LogExporter compositeLogExporter = LogExporter.composite(exportersByNameCopy.values());
      logRecordProcessors.add(
          BatchLogRecordProcessor.builder(compositeLogExporter)
              .setMeterProvider(meterProvider)
              .build());
    }

    return logRecordProcessors;
  }

  // Visible for testing
  static LogLimits configureLogLimits(ConfigProperties config) {
    LogLimitsBuilder builder = LogLimits.builder();

    Integer maxAttrLength = config.getInt("otel.attribute.value.length.limit");
    if (maxAttrLength != null) {
      builder.setMaxAttributeValueLength(maxAttrLength);
    }

    Integer maxAttrs = config.getInt("otel.attribute.count.limit");
    if (maxAttrs != null) {
      builder.setMaxNumberOfAttributes(maxAttrs);
    }

    return builder.build();
  }

  private LoggerProviderConfiguration() {}
}
