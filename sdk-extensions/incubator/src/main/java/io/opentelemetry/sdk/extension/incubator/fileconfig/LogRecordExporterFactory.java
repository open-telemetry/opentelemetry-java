/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Map;

final class LogRecordExporterFactory implements Factory<LogRecordExporterModel, LogRecordExporter> {
  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(LogRecordExporterModel model, DeclarativeConfigContext context) {
    Map.Entry<String, DeclarativeConfigProperties> logRecordExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "log record exporter");
    LogRecordExporter logRecordExporter =
        context.loadComponent(
            LogRecordExporter.class,
            logRecordExporterKeyValue.getKey(),
            logRecordExporterKeyValue.getValue());
    return context.addCloseable(logRecordExporter);
  }
}
