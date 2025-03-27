/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalOtlpFileExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class LogRecordExporterFactory implements Factory<LogRecordExporterModel, LogRecordExporter> {

  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(
      LogRecordExporterModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    OtlpHttpExporterModel otlpHttpModel = model.getOtlpHttp();
    if (otlpHttpModel != null) {
      model.getAdditionalProperties().put("otlp_http", otlpHttpModel);
    }
    OtlpGrpcExporterModel otlpGrpcModel = model.getOtlpGrpc();
    if (otlpGrpcModel != null) {
      model.getAdditionalProperties().put("otlp_grpc", otlpGrpcModel);
    }
    ExperimentalOtlpFileExporterModel otlpFileExporterModel = model.getOtlpFileDevelopment();
    if (otlpFileExporterModel != null) {
      model.getAdditionalProperties().put("otlp_file/development", otlpFileExporterModel);
    }

    ConsoleExporterModel consoleModel = model.getConsole();
    if (consoleModel != null) {
      model.getAdditionalProperties().put("console", consoleModel);
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new DeclarativeConfigException(
            "Invalid configuration - multiple log record exporters set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> exporterKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException("Missing exporter. This is a programming error."));
      LogRecordExporter logRecordExporter =
          FileConfigUtil.loadComponent(
              spiHelper,
              LogRecordExporter.class,
              exporterKeyValue.getKey(),
              exporterKeyValue.getValue());
      return FileConfigUtil.addAndReturn(closeables, logRecordExporter);
    } else {
      throw new DeclarativeConfigException("log record exporter must be set");
    }
  }
}
