/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessorBuilder;
import java.time.Duration;

final class LogRecordProcessorFactory
    implements Factory<LogRecordProcessorModel, LogRecordProcessor> {

  private static final LogRecordProcessorFactory INSTANCE = new LogRecordProcessorFactory();

  private LogRecordProcessorFactory() {}

  static LogRecordProcessorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordProcessor create(
      LogRecordProcessorModel model, DeclarativeConfigContext context) {
    // We don't use the variable till later but call validate first to confirm there are not
    // multiple samplers.
    ConfigKeyValue processorKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "log record processor");

    if (model.getBatch() != null) {
      return createBatchLogRecordProcessor(model.getBatch(), context);
    }
    if (model.getSimple() != null) {
      return createSimpleLogRecordProcessor(model.getSimple(), context);
    }

    return context.loadComponent(LogRecordProcessor.class, processorKeyValue);
  }

  private static LogRecordProcessor createBatchLogRecordProcessor(
      BatchLogRecordProcessorModel batchModel, DeclarativeConfigContext context) {
    LogRecordExporterModel exporterModel =
        FileConfigUtil.requireNonNull(
            batchModel.getExporter(), "batch log record processor exporter");

    LogRecordExporter logRecordExporter =
        LogRecordExporterFactory.getInstance().create(exporterModel, context);
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
    context.setInternalTelemetry(builder::setInternalTelemetryVersion, builder::setMeterProvider);

    return context.addCloseable(builder.build());
  }

  private static LogRecordProcessor createSimpleLogRecordProcessor(
      SimpleLogRecordProcessorModel simpleModel, DeclarativeConfigContext context) {
    LogRecordExporterModel exporterModel =
        FileConfigUtil.requireNonNull(
            simpleModel.getExporter(), "simple log record processor exporter");
    LogRecordExporter logRecordExporter =
        LogRecordExporterFactory.getInstance().create(exporterModel, context);
    SimpleLogRecordProcessorBuilder builder = SimpleLogRecordProcessor.builder(logRecordExporter);
    context.setInternalTelemetry(unused -> {}, builder::setMeterProvider);
    return context.addCloseable(builder.build());
  }
}
