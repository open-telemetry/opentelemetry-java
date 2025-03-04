/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PrometheusMetricExporterModel;
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
      PushMetricExporterModel exporterModel =
          requireNonNull(periodicModel.getExporter(), "periodic metric reader exporter");
      MetricExporter metricExporter =
          MetricExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      PeriodicMetricReaderBuilder builder =
          PeriodicMetricReader.builder(FileConfigUtil.addAndReturn(closeables, metricExporter));
      if (periodicModel.getInterval() != null) {
        builder.setInterval(Duration.ofMillis(periodicModel.getInterval()));
      }
      CardinalityLimitSelector cardinalityLimitSelector = null;
      if (periodicModel.getCardinalityLimits() != null) {
        cardinalityLimitSelector =
            CardinalityLimitsFactory.getInstance()
                .create(periodicModel.getCardinalityLimits(), spiHelper, closeables);
      }
      MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
          MetricReaderAndCardinalityLimits.create(builder.build(), cardinalityLimitSelector);
      return FileConfigUtil.addAndReturn(closeables, readerAndCardinalityLimits);
    }

    PullMetricReaderModel pullModel = model.getPull();
    if (pullModel != null) {
      PullMetricExporterModel exporterModel =
          requireNonNull(pullModel.getExporter(), "pull metric reader exporter");
      PrometheusMetricExporterModel prometheusModel = exporterModel.getPrometheus();
      if (prometheusModel != null) {
        MetricReader metricReader =
            FileConfigUtil.loadComponent(
                spiHelper, MetricReader.class, "prometheus", prometheusModel);
        CardinalityLimitSelector cardinalityLimitSelector = null;
        if (pullModel.getCardinalityLimits() != null) {
          cardinalityLimitSelector =
              CardinalityLimitsFactory.getInstance()
                  .create(pullModel.getCardinalityLimits(), spiHelper, closeables);
        }
        MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
            MetricReaderAndCardinalityLimits.create(metricReader, cardinalityLimitSelector);
        return FileConfigUtil.addAndReturn(closeables, readerAndCardinalityLimits);
      }

      throw new ConfigurationException("prometheus is the only currently supported pull reader");
    }

    throw new ConfigurationException("reader must be set");
  }
}
