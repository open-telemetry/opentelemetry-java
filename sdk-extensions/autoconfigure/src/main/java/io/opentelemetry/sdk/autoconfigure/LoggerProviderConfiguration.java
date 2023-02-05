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
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.io.Closeable;
import java.time.Duration;
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
          logRecordExporterCustomizer,
      List<Closeable> closeables) {

    loggerProviderBuilder.setLogLimits(() -> configureLogLimits(config));

    Map<String, LogRecordExporter> exportersByName =
        configureLogRecordExporters(
            config, serviceClassLoader, logRecordExporterCustomizer, closeables);

    configureLogRecordProcessors(config, exportersByName, meterProvider, closeables)
        .forEach(loggerProviderBuilder::addLogRecordProcessor);
  }

  // Visible for testing
  static List<LogRecordProcessor> configureLogRecordProcessors(
      ConfigProperties config,
      Map<String, LogRecordExporter> exportersByName,
      MeterProvider meterProvider,
      List<Closeable> closeables) {
    Map<String, LogRecordExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<LogRecordProcessor> logRecordProcessors = new ArrayList<>();

    LogRecordExporter exporter = exportersByNameCopy.remove("logging");
    if (exporter != null) {
      LogRecordProcessor logRecordProcessor = SimpleLogRecordProcessor.create(exporter);
      closeables.add(logRecordProcessor);
      logRecordProcessors.add(logRecordProcessor);
    }

    if (!exportersByNameCopy.isEmpty()) {
      LogRecordExporter compositeLogRecordExporter =
          LogRecordExporter.composite(exportersByNameCopy.values());
      LogRecordProcessor logRecordProcessor =
          configureBatchLogRecordProcessor(config, compositeLogRecordExporter, meterProvider);
      closeables.add(logRecordProcessor);
      logRecordProcessors.add(logRecordProcessor);
    }

    return logRecordProcessors;
  }

  // VisibleForTesting
  static BatchLogRecordProcessor configureBatchLogRecordProcessor(
      ConfigProperties config, LogRecordExporter exporter, MeterProvider meterProvider) {
    BatchLogRecordProcessorBuilder builder =
        BatchLogRecordProcessor.builder(exporter).setMeterProvider(meterProvider);

    Duration scheduleDelay = config.getDuration("otel.blrp.schedule.delay");
    if (scheduleDelay != null) {
      builder.setScheduleDelay(scheduleDelay);
    }

    Integer maxQueue = config.getInt("otel.blrp.max.queue.size");
    if (maxQueue != null) {
      builder.setMaxQueueSize(maxQueue);
    }

    Integer maxExportBatch = config.getInt("otel.blrp.max.export.batch.size");
    if (maxExportBatch != null) {
      builder.setMaxExportBatchSize(maxExportBatch);
    }

    Duration timeout = config.getDuration("otel.blrp.export.timeout");
    if (timeout != null) {
      builder.setExporterTimeout(timeout);
    }

    return builder.build();
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
