/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.LogRecordExporterConfiguration.configureLogRecordExporters;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogLimitsBuilder;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;
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
      BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
          logRecordExporterCustomizer) {

    loggerProviderBuilder.setLogLimits(() -> configureLogLimits(config));

    Map<String, LogRecordExporter> exportersByName =
        configureLogRecordExporters(
            config, serviceClassLoader, meterProvider, logRecordExporterCustomizer);

    configureLogProcessors(exportersByName, meterProvider)
        .forEach(loggerProviderBuilder::addLogProcessor);
  }

  // Visible for testing
  static List<LogProcessor> configureLogProcessors(
      Map<String, LogRecordExporter> exportersByName, MeterProvider meterProvider) {
    Map<String, LogRecordExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<LogProcessor> logProcessors = new ArrayList<>();

    LogRecordExporter exporter = exportersByNameCopy.remove("logging");
    if (exporter != null) {
      logProcessors.add(SimpleLogProcessor.create(exporter));
    }

    if (!exportersByNameCopy.isEmpty()) {
      LogRecordExporter compositeLogRecordExporter =
          LogRecordExporter.composite(exportersByNameCopy.values());
      logProcessors.add(
          BatchLogProcessor.builder(compositeLogRecordExporter)
              .setMeterProvider(meterProvider)
              .build());
    }

    return logProcessors;
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
