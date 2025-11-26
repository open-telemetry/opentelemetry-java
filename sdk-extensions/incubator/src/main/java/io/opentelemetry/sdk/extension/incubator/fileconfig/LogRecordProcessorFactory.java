/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

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
    BatchLogRecordProcessorModel batchModel = model.getBatch();
    if (batchModel != null) {
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

    SimpleLogRecordProcessorModel simpleModel = model.getSimple();
    if (simpleModel != null) {
      LogRecordExporterModel exporterModel =
          FileConfigUtil.requireNonNull(
              simpleModel.getExporter(), "simple log record processor exporter");
      LogRecordExporter logRecordExporter =
          LogRecordExporterFactory.getInstance().create(exporterModel, context);
      return context.addCloseable(SimpleLogRecordProcessor.create(logRecordExporter));
    }

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(
            model.getAdditionalProperties(), "log record processor");
    LogRecordProcessor logRecordProcessor =
        context.loadComponent(LogRecordProcessor.class, keyValue.getKey(), keyValue.getValue());
    return context.addCloseable(logRecordProcessor);
  }
}
