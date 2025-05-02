/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

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

    model.getAdditionalProperties().compute("otlp_http", (k, v) -> model.getOtlpHttp());
    model.getAdditionalProperties().compute("otlp_grpc", (k, v) -> model.getOtlpGrpc());
    model
        .getAdditionalProperties()
        .compute("otlp_file/development", (k, v) -> model.getOtlpFileDevelopment());
    model.getAdditionalProperties().compute("console", (k, v) -> model.getConsole());
    model.getAdditionalProperties().compute("zipkin", (k, v) -> model.getZipkin());

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "span exporter");
    SpanExporter metricExporter =
        FileConfigUtil.loadComponent(
            spiHelper, SpanExporter.class, keyValue.getKey(), keyValue.getValue());
    return FileConfigUtil.addAndReturn(closeables, metricExporter);
  }
}
