/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
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

    model.getAdditionalProperties().compute("otlp_http", (v1, v2) -> model.getOtlpHttp());
    model.getAdditionalProperties().compute("otlp_grpc", (v1, v2) -> model.getOtlpGrpc());
    model
        .getAdditionalProperties()
        .compute("otlp_file/development", (v1, v2) -> model.getOtlpFileDevelopment());
    model.getAdditionalProperties().compute("console", (v1, v2) -> model.getConsole());

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
