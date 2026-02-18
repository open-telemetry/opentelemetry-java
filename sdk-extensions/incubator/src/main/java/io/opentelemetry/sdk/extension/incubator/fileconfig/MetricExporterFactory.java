/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.logging.Logger;

final class MetricExporterFactory implements Factory<PushMetricExporterModel, MetricExporter> {
  private static final Logger logger = Logger.getLogger(MetricExporterFactory.class.getName());
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

    // Apply customizers
    DeclarativeConfigurationBuilder builder = context.getBuilder();
    if (builder != null) {
      for (DeclarativeConfigurationBuilder.ExporterCustomizer<MetricExporter> customizerEntry :
          builder.getMetricExporterCustomizers()) {
        if (customizerEntry.getExporterType().isInstance(exporter)) {
          MetricExporter customized =
              customizerEntry.getCustomizer().apply(exporter, metricExporterKeyValue.getValue());
          if (customized == null) {
            logger.warning(
                "Metric exporter customizer returned null for exporter: "
                    + exporterName
                    + ", using original exporter");
          } else {
            exporter = customized;
          }
        }
      }
    }
    return exporter;
  }
}
