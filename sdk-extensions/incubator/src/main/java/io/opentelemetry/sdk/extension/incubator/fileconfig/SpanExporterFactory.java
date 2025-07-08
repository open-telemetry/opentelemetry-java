/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Map;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(SpanExporterModel model, DeclarativeConfigContext context) {

    model.getAdditionalProperties().compute("otlp_http", (k, v) -> model.getOtlpHttp());
    model.getAdditionalProperties().compute("otlp_grpc", (k, v) -> model.getOtlpGrpc());
    model
        .getAdditionalProperties()
        .compute("otlp_file/development", (k, v) -> model.getOtlpFileDevelopment());
    model.getAdditionalProperties().compute("console", (k, v) -> model.getConsole());
    model.getAdditionalProperties().compute("zipkin", (k, v) -> model.getZipkin());

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "span exporter");
    SpanExporter spanExporter =
        context.loadComponent(SpanExporter.class, keyValue.getKey(), keyValue.getValue());
    return context.addCloseable(spanExporter);
  }
}
