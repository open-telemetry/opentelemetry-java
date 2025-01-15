/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpFileExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ZipkinSpanExporterModel;
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
    OtlpHttpExporterModel otlpHttpModel = model.getOtlpHttp();
    if (otlpHttpModel != null) {
      model.getAdditionalProperties().put("otlp_http", otlpHttpModel);
    }
    OtlpGrpcExporterModel otlpGrpcModel = model.getOtlpGrpc();
    if (otlpGrpcModel != null) {
      model.getAdditionalProperties().put("otlp_grpc", otlpGrpcModel);
    }
    OtlpFileExporterModel otlpFileExporterModel = model.getOtlpFile();
    if (model.getOtlpFile() != null) {
      model.getAdditionalProperties().put("otlp_file", otlpFileExporterModel);
    }

    if (model.getConsole() != null) {
      model.getAdditionalProperties().put("console", model.getConsole());
    }

    ZipkinSpanExporterModel zipkinModel = model.getZipkin();
    if (zipkinModel != null) {
      model.getAdditionalProperties().put("zipkin", model.getZipkin());
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new ConfigurationException(
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
      throw new ConfigurationException("span exporter must be set");
    }
  }
}
