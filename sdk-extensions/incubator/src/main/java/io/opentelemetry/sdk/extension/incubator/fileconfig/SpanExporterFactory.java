/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(
      SpanExporterModel model, SpiHelper spiHelper, List<Closeable> closeables) {

    model.getAdditionalProperties().compute("otlp_http", (v1, v2) -> model.getOtlpHttp());
    model.getAdditionalProperties().compute("otlp_grpc", (v1, v2) -> model.getOtlpGrpc());
    model
        .getAdditionalProperties()
        .compute("otlp_file/development", (v1, v2) -> model.getOtlpFileDevelopment());
    model.getAdditionalProperties().compute("console", (v1, v2) -> model.getConsole());
    model.getAdditionalProperties().compute("zipkin", (v1, v2) -> model.getZipkin());

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new DeclarativeConfigException(
            "Invalid configuration - multiple span exporters set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> exporterKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException("Missing exporter. This is a programming error."));
      SpanExporter spanExporter =
          FileConfigUtil.loadComponent(
              spiHelper,
              SpanExporter.class,
              exporterKeyValue.getKey(),
              exporterKeyValue.getValue());
      return FileConfigUtil.addAndReturn(closeables, spanExporter);
    } else {
      throw new DeclarativeConfigException("span exporter must be set");
    }
  }
}
