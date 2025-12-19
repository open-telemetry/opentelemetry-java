/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.LinkedHashMap;
import java.util.Map;

final class LogRecordExporterFactory implements Factory<LogRecordExporterModel, LogRecordExporter> {
  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(LogRecordExporterModel model, DeclarativeConfigContext context) {
    Map<String, Object> exporterResourceByName = new LinkedHashMap<>();

    if (model.getOtlpHttp() != null) {
      exporterResourceByName.put("otlp_http", model.getOtlpHttp());
    }
    if (model.getOtlpGrpc() != null) {
      exporterResourceByName.put("otlp_grpc", model.getOtlpGrpc());
    }
    if (model.getOtlpFileDevelopment() != null) {
      exporterResourceByName.put("otlp_file/development", model.getOtlpFileDevelopment());
    }
    if (model.getConsole() != null) {
      exporterResourceByName.put("console", model.getConsole());
    }
    exporterResourceByName.putAll(model.getAdditionalProperties());

    Map.Entry<String, ?> keyValue =
        FileConfigUtil.getSingletonMapEntry(exporterResourceByName, "log record exporter");
    LogRecordExporter metricExporter =
        context.loadComponent(LogRecordExporter.class, keyValue.getKey(), keyValue.getValue());
    return context.addCloseable(metricExporter);
  }
}
