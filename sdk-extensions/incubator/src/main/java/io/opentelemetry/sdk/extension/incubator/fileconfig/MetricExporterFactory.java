/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNullResource;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Map;

final class MetricExporterFactory implements Factory<PushMetricExporterModel, MetricExporter> {

  private static final String RESOURCE_NAME = "metric exporter";

  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(PushMetricExporterModel model, DeclarativeConfigContext context) {

    String key = null;
    Object resource = null;

    if (model.getOtlpHttp() != null) {
      key = "otlp_http";
      resource = model.getOtlpHttp();
    }
    if (model.getOtlpGrpc() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "otlp_grpc";
      resource = model.getOtlpGrpc();
    }
    if (model.getOtlpFileDevelopment() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "otlp_file/development";
      resource = model.getOtlpFileDevelopment();
    }
    if (model.getConsole() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "console";
      resource = model.getConsole();
    }
    if (key == null || resource == null) {
      Map.Entry<String, ?> keyValue =
          FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), RESOURCE_NAME);
      key = keyValue.getKey();
      resource = keyValue.getValue();
    }

    MetricExporter metricExporter = context.loadComponent(MetricExporter.class, key, resource);
    return context.addCloseable(metricExporter);
  }
}
