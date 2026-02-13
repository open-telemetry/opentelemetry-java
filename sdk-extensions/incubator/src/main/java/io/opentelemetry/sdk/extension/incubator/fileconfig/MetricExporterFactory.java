/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

final class MetricExporterFactory implements Factory<PushMetricExporterModel, MetricExporter> {
  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(PushMetricExporterModel model, DeclarativeConfigContext context) {
    ConfigKeyValue metricExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "metric exporter");

    String exporterName = metricExporterKeyValue.getKey();
    MetricExporter exporter = context.loadComponent(MetricExporter.class, metricExporterKeyValue);

    // Apply customizer
    MetricExporter customized = context.getMetricExporterCustomizer().apply(exporterName, exporter);
    if (customized == null) {
      throw new DeclarativeConfigException(
          "Metric exporter customizer returned null for exporter: " + exporterName);
    }
    return customized;
  }
}
