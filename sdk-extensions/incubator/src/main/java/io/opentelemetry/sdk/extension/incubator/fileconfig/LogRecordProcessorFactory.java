/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.time.Duration;
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
      LogRecordProcessorModel model, DeclarativeConfigContext context) {
    // We don't use the variable till later but call validate first to confirm there are not
    // multiple samplers.
    Map.Entry<String, DeclarativeConfigProperties> processorKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "log record processor");

    if (model.getBatch() != null) {
      return createBatchLogRecordProcessor(model.getBatch(), context);
    }
    if (model.getSimple() != null) {
      return createSimpleLogRecordProcessor(model.getSimple(), context);
    }

    LogRecordProcessor logRecordProcessor =
        context.loadComponent(
            LogRecordProcessor.class, processorKeyValue.getKey(), processorKeyValue.getValue());
    return context.addCloseable(logRecordProcessor);
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
    MeterProvider meterProvider = context.getMeterProvider();
    if (meterProvider != null) {
      builder.setMeterProvider(meterProvider);
    }

    return context.addCloseable(builder.build());
  }

  private static LogRecordProcessor createSimpleLogRecordProcessor(
      SimpleLogRecordProcessorModel simpleModel, DeclarativeConfigContext context) {
    LogRecordExporterModel exporterModel =
        FileConfigUtil.requireNonNull(
            simpleModel.getExporter(), "simple log record processor exporter");
    LogRecordExporter logRecordExporter =
        LogRecordExporterFactory.getInstance().create(exporterModel, context);
    MeterProvider meterProvider = context.getMeterProvider();
    return context.addCloseable(
        SimpleLogRecordProcessor.builder(logRecordExporter)
            .setMeterProvider(() -> meterProvider)
            .build());
  }
}
