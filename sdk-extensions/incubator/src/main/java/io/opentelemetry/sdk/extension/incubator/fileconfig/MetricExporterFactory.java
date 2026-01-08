/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.LinkedHashMap;
import java.util.Map;

final class MetricExporterFactory implements Factory<PushMetricExporterModel, MetricExporter> {
  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(PushMetricExporterModel model, DeclarativeConfigContext context) {
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

    Map.Entry<String, ?> keyValue =
        FileConfigUtil.getSingletonMapEntry(exporterResourceByName, "metric exporter");
    MetricExporter metricExporter =
        context.loadComponent(MetricExporter.class, keyValue.getKey(), keyValue.getValue());
    return context.addCloseable(metricExporter);
  }
}
