/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.LogRecordProcessorModelAccessor;
import io.opentelemetry.sdk.extension.incubator.logs.EventToSpanEventBridge;
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

    BatchLogRecordProcessorModel batch = model.getBatch();
    if (batch != null) {
      return context.withConfigPath("batch", () -> createBatchLogRecordProcessor(batch, context));
    }
    SimpleLogRecordProcessorModel simple = model.getSimple();
    if (simple != null) {
      return context.withConfigPath(
          "simple", () -> createSimpleLogRecordProcessor(simple, context));
    }
    if (LogRecordProcessorModelAccessor.getEventToSpanEventBridge(model) != null) {
      return EventToSpanEventBridge.create();
    }

    return context.loadComponent(LogRecordProcessor.class, processorKeyValue);
  }

  private static LogRecordProcessor createBatchLogRecordProcessor(
      BatchLogRecordProcessorModel batchModel, DeclarativeConfigContext context) {
    LogRecordExporterModel exporterModel =
        FileConfigUtil.requireNonNull(context, batchModel.getExporter(), "exporter");

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
    context.setInternalTelemetry(builder::setMeterProvider, builder::setInternalTelemetryVersion);

    return context.addCloseable(builder.build());
  }

  private static LogRecordProcessor createSimpleLogRecordProcessor(
      SimpleLogRecordProcessorModel simpleModel, DeclarativeConfigContext context) {
    LogRecordExporterModel exporterModel =
        FileConfigUtil.requireNonNull(context, simpleModel.getExporter(), "exporter");
    LogRecordExporter logRecordExporter =
        LogRecordExporterFactory.getInstance().create(exporterModel, context);
    SimpleLogRecordProcessorBuilder builder = SimpleLogRecordProcessor.builder(logRecordExporter);
    context.setInternalTelemetry(builder::setMeterProvider);
    return context.addCloseable(builder.build());
  }
}
