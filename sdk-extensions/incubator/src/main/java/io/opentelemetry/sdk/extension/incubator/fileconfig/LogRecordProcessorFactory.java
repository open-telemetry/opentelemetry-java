/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Map;

final class LogRecordProcessorFactory
    implements Factory<LogRecordProcessorModel, LogRecordProcessor> {

  private static final LogRecordProcessorFactory INSTANCE = new LogRecordProcessorFactory();

  private LogRecordProcessorFactory() {}

  static LogRecordProcessorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordProcessor create(
      LogRecordProcessorModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    BatchLogRecordProcessorModel batchModel = model.getBatch();
    if (batchModel != null) {
      LogRecordExporterModel exporterModel =
          FileConfigUtil.requireNonNull(
              batchModel.getExporter(), "batch log record processor exporter");

      io.opentelemetry.sdk.logs.export.LogRecordExporter logRecordExporter =
          LogRecordExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      BatchLogRecordProcessorBuilder builder = BatchLogRecordProcessor.builder(logRecordExporter);
      if (batchModel.getExportTimeout() != null) {
        builder.setExporterTimeout(Duration.ofMillis(batchModel.getExportTimeout()));
      }
      if (batchModel.getMaxExportBatchSize() != null) {
        builder.setMaxExportBatchSize(batchModel.getMaxExportBatchSize());
      }
      if (batchModel.getMaxQueueSize() != null) {
        builder.setMaxQueueSize(batchModel.getMaxQueueSize());
      }
      if (batchModel.getScheduleDelay() != null) {
        builder.setScheduleDelay(Duration.ofMillis(batchModel.getScheduleDelay()));
      }
      return FileConfigUtil.addAndReturn(closeables, builder.build());
    }

    SimpleLogRecordProcessorModel simpleModel = model.getSimple();
    if (simpleModel != null) {
      LogRecordExporterModel exporterModel =
          FileConfigUtil.requireNonNull(
              simpleModel.getExporter(), "simple log record processor exporter");
      io.opentelemetry.sdk.logs.export.LogRecordExporter logRecordExporter =
          LogRecordExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      return FileConfigUtil.addAndReturn(
          closeables, SimpleLogRecordProcessor.create(logRecordExporter));
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new DeclarativeConfigException(
            "Invalid configuration - multiple log record processors set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> processorKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException("Missing processor. This is a programming error."));
      LogRecordProcessor logRecordProcessor =
          FileConfigUtil.loadComponent(
              spiHelper,
              LogRecordProcessor.class,
              processorKeyValue.getKey(),
              processorKeyValue.getValue());
      return FileConfigUtil.addAndReturn(closeables, logRecordProcessor);
    } else {
      throw new DeclarativeConfigException("log processor must be set");
    }
  }
}
