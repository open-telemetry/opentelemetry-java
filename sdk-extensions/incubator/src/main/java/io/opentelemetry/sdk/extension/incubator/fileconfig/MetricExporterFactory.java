/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Map;

final class MetricExporterFactory implements Factory<PushMetricExporterModel, MetricExporter> {
  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(PushMetricExporterModel model, DeclarativeConfigContext context) {
    Map.Entry<String, DeclarativeConfigProperties> metricExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "metric exporter");
    return context.loadComponent(
        MetricExporter.class, metricExporterKeyValue.getKey(), metricExporterKeyValue.getValue());
  }
}
