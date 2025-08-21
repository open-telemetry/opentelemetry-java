/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

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

    model.getAdditionalProperties().compute("otlp_http", (k, v) -> model.getOtlpHttp());
    model.getAdditionalProperties().compute("otlp_grpc", (k, v) -> model.getOtlpGrpc());
    model
        .getAdditionalProperties()
        .compute("otlp_file/development", (k, v) -> model.getOtlpFileDevelopment());
    model.getAdditionalProperties().compute("console", (k, v) -> model.getConsole());

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "log record exporter");
    LogRecordExporter logRecordExporter =
        context.loadComponent(
            LogRecordExporter.class, keyValue.getKey(), keyValue.getValue());
    return context.addCloseable(logRecordExporter);
  }
}
