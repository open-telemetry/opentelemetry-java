/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReaderBuilder;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import javax.annotation.Nullable;

final class MetricReaderFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader,
        MetricReader> {

  private static final MetricReaderFactory INSTANCE = new MetricReaderFactory();

  private MetricReaderFactory() {}

  static MetricReaderFactory getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("NullAway") // Override superclass non-null response
  @Override
  @Nullable
  public MetricReader create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return null;
    }

    PeriodicMetricReader periodicModel = model.getPeriodic();
    if (periodicModel != null) {
      MetricExporter exporterModel = periodicModel.getExporter();
      if (exporterModel == null) {
        throw new ConfigurationException("exporter required for periodic reader");
      }
      io.opentelemetry.sdk.metrics.export.MetricExporter metricExporter =
          MetricExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      if (metricExporter == null) {
        return null;
      }
      PeriodicMetricReaderBuilder builder =
          io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
              FileConfigUtil.addAndReturn(closeables, metricExporter));
      if (periodicModel.getInterval() != null) {
        builder.setInterval(Duration.ofMillis(periodicModel.getInterval()));
      }
      return FileConfigUtil.addAndReturn(closeables, builder.build());
    }

    PullMetricReader pullModel = model.getPull();
    if (pullModel != null) {
      MetricExporter exporterModel = pullModel.getExporter();
      if (exporterModel == null) {
        throw new ConfigurationException("exporter required for pull reader");
      }
      Prometheus prometheusModel = exporterModel.getPrometheus();
      if (prometheusModel != null) {
        MetricReader metricReader =
            FileConfigUtil.loadComponent(
                spiHelper, MetricReader.class, "prometheus", prometheusModel);
        return FileConfigUtil.addAndReturn(closeables, metricReader);
      }

      throw new ConfigurationException("prometheus is the only currently supported pull reader");
    }

    return null;
  }
}
