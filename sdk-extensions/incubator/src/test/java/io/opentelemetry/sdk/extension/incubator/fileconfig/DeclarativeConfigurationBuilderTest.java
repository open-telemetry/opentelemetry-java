/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;

class DeclarativeConfigurationBuilderTest {

  @Test
  void spanExporterCustomizer_Single() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    SpanExporter mockExporter = mock(SpanExporter.class);

    builder.addSpanExporterCustomizer((name, exporter) -> mockExporter);

    SpanExporter result =
        builder.getSpanExporterCustomizer().apply("test", mock(SpanExporter.class));
    assertThat(result).isSameAs(mockExporter);
  }

  @Test
  void spanExporterCustomizer_Multiple_Compose() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    SpanExporter originalExporter = mock(SpanExporter.class);
    SpanExporter firstResult = mock(SpanExporter.class);
    SpanExporter secondResult = mock(SpanExporter.class);

    builder.addSpanExporterCustomizer(
        (name, exporter) -> {
          assertThat(exporter).isSameAs(originalExporter);
          return firstResult;
        });

    builder.addSpanExporterCustomizer(
        (name, exporter) -> {
          assertThat(exporter).isSameAs(firstResult);
          return secondResult;
        });

    SpanExporter result = builder.getSpanExporterCustomizer().apply("test", originalExporter);
    assertThat(result).isSameAs(secondResult);
  }

  @Test
  void metricExporterCustomizer_Single() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    MetricExporter mockExporter = mock(MetricExporter.class);

    builder.addMetricExporterCustomizer((name, exporter) -> mockExporter);

    MetricExporter result =
        builder.getMetricExporterCustomizer().apply("test", mock(MetricExporter.class));
    assertThat(result).isSameAs(mockExporter);
  }

  @Test
  void metricExporterCustomizer_Multiple_Compose() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    MetricExporter originalExporter = mock(MetricExporter.class);
    MetricExporter firstResult = mock(MetricExporter.class);
    MetricExporter secondResult = mock(MetricExporter.class);

    builder.addMetricExporterCustomizer(
        (name, exporter) -> {
          assertThat(exporter).isSameAs(originalExporter);
          return firstResult;
        });

    builder.addMetricExporterCustomizer(
        (name, exporter) -> {
          assertThat(exporter).isSameAs(firstResult);
          return secondResult;
        });

    MetricExporter result = builder.getMetricExporterCustomizer().apply("test", originalExporter);
    assertThat(result).isSameAs(secondResult);
  }

  @Test
  void logRecordExporterCustomizer_Single() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    LogRecordExporter mockExporter = mock(LogRecordExporter.class);

    builder.addLogRecordExporterCustomizer((name, exporter) -> mockExporter);

    LogRecordExporter result =
        builder.getLogRecordExporterCustomizer().apply("test", mock(LogRecordExporter.class));
    assertThat(result).isSameAs(mockExporter);
  }

  @Test
  void logRecordExporterCustomizer_Multiple_Compose() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    LogRecordExporter originalExporter = mock(LogRecordExporter.class);
    LogRecordExporter firstResult = mock(LogRecordExporter.class);
    LogRecordExporter secondResult = mock(LogRecordExporter.class);

    builder.addLogRecordExporterCustomizer(
        (name, exporter) -> {
          assertThat(exporter).isSameAs(originalExporter);
          return firstResult;
        });

    builder.addLogRecordExporterCustomizer(
        (name, exporter) -> {
          assertThat(exporter).isSameAs(firstResult);
          return secondResult;
        });

    LogRecordExporter result =
        builder.getLogRecordExporterCustomizer().apply("test", originalExporter);
    assertThat(result).isSameAs(secondResult);
  }
}
