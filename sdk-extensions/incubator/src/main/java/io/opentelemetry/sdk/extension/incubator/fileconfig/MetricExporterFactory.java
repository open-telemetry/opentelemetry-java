/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class MetricExporterFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter,
        MetricExporter> {

  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(
      io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    OtlpMetric otlpModel = model.getOtlp();
    if (otlpModel != null) {
      model.getAdditionalProperties().put("otlp", otlpModel);
    }

    if (model.getConsole() != null) {
      model.getAdditionalProperties().put("console", model.getConsole());
    }

    if (model.getPrometheus() != null) {
      throw new DeclarativeConfigException("prometheus exporter not supported in this context");
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new DeclarativeConfigException(
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
      throw new DeclarativeConfigException("metric exporter must be set");
    }
  }
}
