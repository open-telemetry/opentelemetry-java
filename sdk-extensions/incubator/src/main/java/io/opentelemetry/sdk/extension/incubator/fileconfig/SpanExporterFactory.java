/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Zipkin;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class SpanExporterFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter,
        SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(
      io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    Otlp otlpModel = model.getOtlp();
    if (otlpModel != null) {
      model.getAdditionalProperties().put("otlp", otlpModel);
    }

    if (model.getConsole() != null) {
      model.getAdditionalProperties().put("console", model.getConsole());
    }

    Zipkin zipkinModel = model.getZipkin();
    if (zipkinModel != null) {
      model.getAdditionalProperties().put("zipkin", model.getZipkin());
    }

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
