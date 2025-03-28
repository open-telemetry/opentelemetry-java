/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.LogRecordExporterConfiguration.configureLogRecordExporters;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogSamplerProvider;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogLimitsBuilder;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.samplers.LogSampler;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

final class LoggerProviderConfiguration {

  private static final List<String> simpleProcessorExporterNames =
      Arrays.asList("console", "logging");
  public static final String ALWAYS_ON = "always_on";

  static void configureLoggerProvider(
      SdkLoggerProviderBuilder loggerProviderBuilder,
      ConfigProperties config,
      SpiHelper spiHelper,
      MeterProvider meterProvider,
      BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
          logRecordExporterCustomizer,
      BiFunction<? super LogRecordProcessor, ConfigProperties, ? extends LogRecordProcessor>
          logRecordProcessorCustomizer,
      List<Closeable> closeables) {

    loggerProviderBuilder.setLogLimits(() -> configureLogLimits(config));

    Map<String, LogRecordExporter> exportersByName =
        configureLogRecordExporters(config, spiHelper, logRecordExporterCustomizer, closeables);

    String sampler = config.getString("otel.logs.sampler", ALWAYS_ON);
    loggerProviderBuilder.setLogSampler(configSampler(sampler, config, spiHelper));

    List<LogRecordProcessor> processors =
        configureLogRecordProcessors(config, exportersByName, meterProvider, closeables);
    for (LogRecordProcessor processor : processors) {
      LogRecordProcessor wrapped = logRecordProcessorCustomizer.apply(processor, config);
      if (wrapped != processor) {
        closeables.add(wrapped);
      }
      loggerProviderBuilder.addLogRecordProcessor(wrapped);
    }
  }

  static LogSampler configSampler(String sampler, ConfigProperties config, SpiHelper spiHelper) {
    NamedSpiManager<LogSampler> spiSamplersManager =
        spiHelper.loadConfigurable(
            ConfigurableLogSamplerProvider.class,
            ConfigurableLogSamplerProvider::getName,
            ConfigurableLogSamplerProvider::createSampler,
            config);
    switch (sampler) {
      case "always_on":
        return LogSampler.alwaysOnSampler();
      case "parentbased":
        return LogSampler.parentBasedSampler();
      default:
        LogSampler spiSampler = spiSamplersManager.getByName(sampler);
        if (spiSampler == null) {
          throw new ConfigurationException("Unrecognized value for otel.logs.sampler: " + sampler);
        }
        return spiSampler;
    }
  }

  // Visible for testing
  static List<LogRecordProcessor> configureLogRecordProcessors(
      ConfigProperties config,
      Map<String, LogRecordExporter> exportersByName,
      MeterProvider meterProvider,
      List<Closeable> closeables) {
    Map<String, LogRecordExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<LogRecordProcessor> logRecordProcessors = new ArrayList<>();

    for (String simpleProcessorExporterName : simpleProcessorExporterNames) {
      LogRecordExporter exporter = exportersByNameCopy.remove(simpleProcessorExporterName);
      if (exporter != null) {
        LogRecordProcessor logRecordProcessor = SimpleLogRecordProcessor.create(exporter);
        closeables.add(logRecordProcessor);
        logRecordProcessors.add(logRecordProcessor);
      }
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
