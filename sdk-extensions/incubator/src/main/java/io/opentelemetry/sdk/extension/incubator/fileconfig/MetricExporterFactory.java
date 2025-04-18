/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
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

    model.getAdditionalProperties().compute("otlp_http", (k, v) -> model.getOtlpHttp());
    model.getAdditionalProperties().compute("otlp_grpc", (k, v) -> model.getOtlpGrpc());
    model
        .getAdditionalProperties()
        .compute("otlp_file/development", (k, v) -> model.getOtlpFileDevelopment());
    model.getAdditionalProperties().compute("console", (k, v) -> model.getConsole());

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "metric exporter");
    MetricExporter metricExporter =
        FileConfigUtil.loadComponent(
            spiHelper, MetricExporter.class, keyValue.getKey(), keyValue.getValue());
    return FileConfigUtil.addAndReturn(closeables, metricExporter);
  }
}
