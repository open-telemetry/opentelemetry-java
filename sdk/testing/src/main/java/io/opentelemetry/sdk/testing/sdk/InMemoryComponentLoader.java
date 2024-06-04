/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.sdk;

import io.opentelemetry.sdk.autoconfigure.internal.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.List;

class InMemoryComponentLoader implements ComponentLoader {
  private final SpiHelper spiHelper =
      SpiHelper.create(AutoConfiguredOpenTelemetryTesting.class.getClassLoader());
  private final InMemorySpanExporter spanExporter;
  private final InMemoryMetricExporter metricExporter;
  private final InMemoryLogRecordExporter logRecordExporter;

  public InMemoryComponentLoader(
      InMemorySpanExporter spanExporter,
      InMemoryMetricExporter metricExporter,
      InMemoryLogRecordExporter logRecordExporter) {
    this.spanExporter = spanExporter;
    this.metricExporter = metricExporter;
    this.logRecordExporter = logRecordExporter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> load(Class<T> spiClass) {
    List<T> list = spiHelper.load(spiClass);
    if (spiClass == ConfigurableSpanExporterProvider.class) {
      list.addAll(
          (List<T>)
              Collections.<ConfigurableSpanExporterProvider>singletonList(
                  new ConfigurableSpanExporterProvider() {
                    @Override
                    public SpanExporter createExporter(ConfigProperties configProperties) {
                      return spanExporter;
                    }

                    @Override
                    public String getName() {
                      return AutoConfiguredOpenTelemetryTesting.MEMORY_EXPORTER;
                    }
                  }));
    }
    if (spiClass == ConfigurableMetricExporterProvider.class) {
      list.addAll(
          (List<T>)
              Collections.<ConfigurableMetricExporterProvider>singletonList(
                  new ConfigurableMetricExporterProvider() {
                    @Override
                    public MetricExporter createExporter(ConfigProperties configProperties) {
                      return metricExporter;
                    }

                    @Override
                    public String getName() {
                      return AutoConfiguredOpenTelemetryTesting.MEMORY_EXPORTER;
                    }
                  }));
    }
    if (spiClass == ConfigurableLogRecordExporterProvider.class) {
      list.addAll(
          (List<T>)
              Collections.<ConfigurableLogRecordExporterProvider>singletonList(
                  new ConfigurableLogRecordExporterProvider() {
                    @Override
                    public InMemoryLogRecordExporter createExporter(
                        ConfigProperties configProperties) {
                      return logRecordExporter;
                    }

                    @Override
                    public String getName() {
                      return AutoConfiguredOpenTelemetryTesting.MEMORY_EXPORTER;
                    }
                  }));
    }
    return list;
  }
}
