/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.LinkedHashMap;
import java.util.Map;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(SpanExporterModel model, DeclarativeConfigContext context) {
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

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(exporterResourceByName, "span exporter");
    SpanExporter spanExporter =
        context.loadComponent(SpanExporter.class, keyValue.getKey(), keyValue.getValue());
    return context.addCloseable(spanExporter);
  }
}
