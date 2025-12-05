/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNullResource;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Map;

final class LogRecordExporterFactory implements Factory<LogRecordExporterModel, LogRecordExporter> {

  private static final String RESOURCE_NAME = "log record exporter";

  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(LogRecordExporterModel model, DeclarativeConfigContext context) {

    String key = null;
    Object resource = null;

    if (model.getOtlpHttp() != null) {
      key = "otlp_http";
      resource = model.getOtlpHttp();
    }
    if (model.getOtlpGrpc() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "otlp_grpc";
      resource = model.getOtlpGrpc();
    }
    if (model.getOtlpFileDevelopment() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "otlp_file/development";
      resource = model.getOtlpFileDevelopment();
    }
    if (model.getConsole() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "console";
      resource = model.getConsole();
    }
    if (key == null || resource == null) {
      Map.Entry<String, ?> keyValue =
          FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), RESOURCE_NAME);
      key = keyValue.getKey();
      resource = keyValue.getValue();
    }

    LogRecordExporter logRecordExporter =
        context.loadComponent(LogRecordExporter.class, key, resource);
    return context.addCloseable(logRecordExporter);
  }
}
