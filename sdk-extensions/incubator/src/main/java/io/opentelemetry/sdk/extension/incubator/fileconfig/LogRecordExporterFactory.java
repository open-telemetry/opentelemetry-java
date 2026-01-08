/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

final class LogRecordExporterFactory implements Factory<LogRecordExporterModel, LogRecordExporter> {
  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(LogRecordExporterModel model, DeclarativeConfigContext context) {
    ConfigKeyValue logRecordExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "log record exporter");
    return context.loadComponent(LogRecordExporter.class, logRecordExporterKeyValue);
  }
}
