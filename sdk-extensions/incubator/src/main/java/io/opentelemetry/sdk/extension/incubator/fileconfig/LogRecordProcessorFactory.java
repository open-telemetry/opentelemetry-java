/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import javax.annotation.Nullable;

final class LogRecordProcessorFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor,
        LogRecordProcessor> {

  private static final LogRecordProcessorFactory INSTANCE = new LogRecordProcessorFactory();

  private LogRecordProcessorFactory() {}

  static LogRecordProcessorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordProcessor create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor
              model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return LogRecordProcessor.composite();
    }

    io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor
        batchModel = model.getBatch();
    if (batchModel != null) {
      LogRecordExporter exporterModel = batchModel.getExporter();
      io.opentelemetry.sdk.logs.export.LogRecordExporter logRecordExporter =
          LogRecordExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      if (logRecordExporter == null) {
        throw new ConfigurationException("exporter required for batch log record processor");
      }
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

    io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessor
        simpleModel = model.getSimple();
    if (simpleModel != null) {
      LogRecordExporter exporterModel = simpleModel.getExporter();
      io.opentelemetry.sdk.logs.export.LogRecordExporter logRecordExporter =
          LogRecordExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      if (logRecordExporter == null) {
        throw new ConfigurationException("exporter required for simple log record processor");
      }
      return FileConfigUtil.addAndReturn(
          closeables, SimpleLogRecordProcessor.create(logRecordExporter));
    }

    // TODO: add support for generic log record processors
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new ConfigurationException(
          "Unrecognized log record processor(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    }

    return LogRecordProcessor.composite();
  }
}
