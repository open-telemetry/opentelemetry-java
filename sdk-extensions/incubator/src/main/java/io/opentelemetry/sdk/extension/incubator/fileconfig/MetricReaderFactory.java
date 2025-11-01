/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalPrometheusMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReaderBuilder;
import java.time.Duration;

final class MetricReaderFactory
    implements Factory<MetricReaderModel, MetricReaderAndCardinalityLimits> {

  private static final MetricReaderFactory INSTANCE = new MetricReaderFactory();

  private MetricReaderFactory() {}

  static MetricReaderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricReaderAndCardinalityLimits create(
      MetricReaderModel model, DeclarativeConfigContext context) {
    PeriodicMetricReaderModel periodicModel = model.getPeriodic();
    if (periodicModel != null) {
      return PeriodicMetricReaderFactory.INSTANCE.create(periodicModel, context);
    }

    PullMetricReaderModel pullModel = model.getPull();
    if (pullModel != null) {
      return PullMetricReaderFactory.INSTANCE.create(pullModel, context);
    }

    throw new DeclarativeConfigException("reader must be set");
  }

  private static class PeriodicMetricReaderFactory
      implements Factory<PeriodicMetricReaderModel, MetricReaderAndCardinalityLimits> {

    private static final PeriodicMetricReaderFactory INSTANCE = new PeriodicMetricReaderFactory();

    private PeriodicMetricReaderFactory() {}

    @Override
    public MetricReaderAndCardinalityLimits create(
        PeriodicMetricReaderModel model, DeclarativeConfigContext context) {
      PushMetricExporterModel exporterModel =
          requireNonNull(model.getExporter(), "periodic metric reader exporter");
      MetricExporter metricExporter =
          MetricExporterFactory.getInstance().create(exporterModel, context);

      PeriodicMetricReaderBuilder builder =
          PeriodicMetricReader.builder(context.addCloseable(metricExporter));

      if (model.getInterval() != null) {
        builder.setInterval(Duration.ofMillis(model.getInterval()));
      }
      CardinalityLimitSelector cardinalityLimitSelector = null;
      if (model.getCardinalityLimits() != null) {
        cardinalityLimitSelector =
            CardinalityLimitsFactory.getInstance().create(model.getCardinalityLimits(), context);
      }

      MetricReader reader = context.addCloseable(builder.build());
      return MetricReaderAndCardinalityLimits.create(reader, cardinalityLimitSelector);
    }
  }

  private static class PullMetricReaderFactory
      implements Factory<PullMetricReaderModel, MetricReaderAndCardinalityLimits> {

    private static final PullMetricReaderFactory INSTANCE = new PullMetricReaderFactory();

    private PullMetricReaderFactory() {}

    @Override
    public MetricReaderAndCardinalityLimits create(
        PullMetricReaderModel model, DeclarativeConfigContext context) {
      PullMetricExporterModel exporterModel =
          requireNonNull(model.getExporter(), "pull metric reader exporter");

      ExperimentalPrometheusMetricExporterModel prometheusModel =
          exporterModel.getPrometheusDevelopment();

      if (prometheusModel != null) {
        MetricReader metricReader =
            context.addCloseable(
                context.loadComponent(
                    MetricReader.class, "prometheus/development", prometheusModel));
        CardinalityLimitSelector cardinalityLimitSelector = null;
        if (model.getCardinalityLimits() != null) {
          cardinalityLimitSelector =
              CardinalityLimitsFactory.getInstance().create(model.getCardinalityLimits(), context);
        }

        return MetricReaderAndCardinalityLimits.create(metricReader, cardinalityLimitSelector);
      }

      throw new DeclarativeConfigException(
          "prometheus is the only currently supported pull reader");
    }
  }
}
