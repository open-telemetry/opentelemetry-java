/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReaderBuilder;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;

final class MetricReaderFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader,
        MetricReader> {

  private static final MetricReaderFactory INSTANCE = new MetricReaderFactory();

  private MetricReaderFactory() {}

  static MetricReaderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricReader create(
      io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    PeriodicMetricReader periodicModel = model.getPeriodic();
    if (periodicModel != null) {
      MetricExporter exporterModel =
          requireNonNull(periodicModel.getExporter(), "periodic metric reader exporter");
      io.opentelemetry.sdk.metrics.export.MetricExporter metricExporter =
          MetricExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
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
      MetricExporter exporterModel =
          requireNonNull(pullModel.getExporter(), "pull metric reader exporter");
      Prometheus prometheusModel = exporterModel.getPrometheus();
      if (prometheusModel != null) {
        MetricReader metricReader =
            FileConfigUtil.loadComponent(
                spiHelper, MetricReader.class, "prometheus", prometheusModel);
        return FileConfigUtil.addAndReturn(closeables, metricReader);
      }

      throw new StructuredConfigException("prometheus is the only currently supported pull reader");
    }

    throw new StructuredConfigException("reader must be set");
  }
}
