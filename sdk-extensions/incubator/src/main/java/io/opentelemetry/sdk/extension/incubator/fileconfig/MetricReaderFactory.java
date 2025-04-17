/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
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
import java.io.Closeable;
import java.time.Duration;
import java.util.List;

final class MetricReaderFactory
    implements Factory<MetricReaderModel, MetricReaderAndCardinalityLimits> {

  private static final MetricReaderFactory INSTANCE = new MetricReaderFactory();

  private MetricReaderFactory() {}

  static MetricReaderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricReaderAndCardinalityLimits create(
      MetricReaderModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    PeriodicMetricReaderModel periodicModel = model.getPeriodic();
    if (periodicModel != null) {
      return PeriodicMetricReaderFactory.INSTANCE.create(periodicModel, spiHelper, closeables);
    }

    PullMetricReaderModel pullModel = model.getPull();
    if (pullModel != null) {
      return PullMetricReaderFactory.INSTANCE.create(pullModel, spiHelper, closeables);
    }

    throw new DeclarativeConfigException("reader must be set");
  }

  private static class PeriodicMetricReaderFactory
      implements Factory<PeriodicMetricReaderModel, MetricReaderAndCardinalityLimits> {

    private static final PeriodicMetricReaderFactory INSTANCE = new PeriodicMetricReaderFactory();

    private PeriodicMetricReaderFactory() {}

    @Override
    public MetricReaderAndCardinalityLimits create(
        PeriodicMetricReaderModel model, SpiHelper spiHelper, List<Closeable> closeables) {
      PushMetricExporterModel exporterModel =
          requireNonNull(model.getExporter(), "periodic metric reader exporter");
      MetricExporter metricExporter =
          MetricExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);

      PeriodicMetricReaderBuilder builder =
          PeriodicMetricReader.builder(FileConfigUtil.addAndReturn(closeables, metricExporter));

      if (model.getInterval() != null) {
        builder.setInterval(Duration.ofMillis(model.getInterval()));
      }
      CardinalityLimitSelector cardinalityLimitSelector = null;
      if (model.getCardinalityLimits() != null) {
        cardinalityLimitSelector =
            CardinalityLimitsFactory.getInstance()
                .create(model.getCardinalityLimits(), spiHelper, closeables);
      }

      MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
          MetricReaderAndCardinalityLimits.create(builder.build(), cardinalityLimitSelector);
      return FileConfigUtil.addAndReturn(closeables, readerAndCardinalityLimits);
    }
  }

  private static class PullMetricReaderFactory
      implements Factory<PullMetricReaderModel, MetricReaderAndCardinalityLimits> {

    private static final PullMetricReaderFactory INSTANCE = new PullMetricReaderFactory();

    private PullMetricReaderFactory() {}

    @Override
    public MetricReaderAndCardinalityLimits create(
        PullMetricReaderModel model, SpiHelper spiHelper, List<Closeable> closeables) {
      PullMetricExporterModel exporterModel =
          requireNonNull(model.getExporter(), "pull metric reader exporter");

      ExperimentalPrometheusMetricExporterModel prometheusModel =
          exporterModel.getPrometheusDevelopment();

      if (prometheusModel != null) {
        MetricReader metricReader =
            FileConfigUtil.loadComponent(
                spiHelper, MetricReader.class, "prometheus", prometheusModel);
        CardinalityLimitSelector cardinalityLimitSelector = null;
        if (model.getCardinalityLimits() != null) {
          cardinalityLimitSelector =
              CardinalityLimitsFactory.getInstance()
                  .create(model.getCardinalityLimits(), spiHelper, closeables);
        }

        MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
            MetricReaderAndCardinalityLimits.create(metricReader, cardinalityLimitSelector);
        return FileConfigUtil.addAndReturn(closeables, readerAndCardinalityLimits);
      }

      throw new DeclarativeConfigException(
          "prometheus is the only currently supported pull reader");
    }
  }
}
