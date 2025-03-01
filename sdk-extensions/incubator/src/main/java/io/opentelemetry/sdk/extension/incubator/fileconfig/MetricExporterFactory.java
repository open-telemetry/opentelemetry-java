/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpFileMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class MetricExporterFactory implements Factory<PushMetricExporterModel, MetricExporter> {

  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(
      PushMetricExporterModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    OtlpHttpMetricExporterModel otlpHttpModel = model.getOtlpHttp();
    if (otlpHttpModel != null) {
      model.getAdditionalProperties().put("otlp_http", otlpHttpModel);
    }
    OtlpGrpcMetricExporterModel otlpGrpcModel = model.getOtlpGrpc();
    if (otlpGrpcModel != null) {
      model.getAdditionalProperties().put("otlp_grpc", otlpGrpcModel);
    }
    OtlpFileMetricExporterModel otlpFileExporterModel = model.getOtlpFile();
    if (model.getOtlpFile() != null) {
      model.getAdditionalProperties().put("otlp_file", otlpFileExporterModel);
    }

    if (model.getConsole() != null) {
      model.getAdditionalProperties().put("console", model.getConsole());
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new ConfigurationException(
            "Invalid configuration - multiple metric exporters set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> exporterKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException("Missing exporter. This is a programming error."));
      MetricExporter metricExporter =
          FileConfigUtil.loadComponent(
              spiHelper,
              MetricExporter.class,
              exporterKeyValue.getKey(),
              exporterKeyValue.getValue());
      return FileConfigUtil.addAndReturn(closeables, metricExporter);
    } else {
      throw new ConfigurationException("metric exporter must be set");
    }
  }
}
