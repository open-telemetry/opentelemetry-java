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

    builder.addSpanExporterCustomizer(
        SpanExporter.class, (exporter, properties) -> mock(SpanExporter.class));

    assertThat(builder.getSpanExporterCustomizers()).hasSize(1);
  }

  @Test
  void spanExporterCustomizer_Multiple_Compose() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();

    builder.addSpanExporterCustomizer(
        SpanExporter.class, (exporter, properties) -> mock(SpanExporter.class));
    builder.addSpanExporterCustomizer(
        SpanExporter.class, (exporter, properties) -> mock(SpanExporter.class));

    assertThat(builder.getSpanExporterCustomizers()).hasSize(2);
  }

  @Test
  void metricExporterCustomizer_Single() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();

    builder.addMetricExporterCustomizer(
        MetricExporter.class, (exporter, properties) -> mock(MetricExporter.class));

    assertThat(builder.getMetricExporterCustomizers()).hasSize(1);
  }

  @Test
  void metricExporterCustomizer_Multiple_Compose() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();

    builder.addMetricExporterCustomizer(
        MetricExporter.class, (exporter, properties) -> mock(MetricExporter.class));
    builder.addMetricExporterCustomizer(
        MetricExporter.class, (exporter, properties) -> mock(MetricExporter.class));

    assertThat(builder.getMetricExporterCustomizers()).hasSize(2);
  }

  @Test
  void logRecordExporterCustomizer_Single() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();

    builder.addLogRecordExporterCustomizer(
        LogRecordExporter.class, (exporter, properties) -> mock(LogRecordExporter.class));

    assertThat(builder.getLogRecordExporterCustomizers()).hasSize(1);
  }

  @Test
  void logRecordExporterCustomizer_Multiple_Compose() {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();

    builder.addLogRecordExporterCustomizer(
        LogRecordExporter.class, (exporter, properties) -> mock(LogRecordExporter.class));
    builder.addLogRecordExporterCustomizer(
        LogRecordExporter.class, (exporter, properties) -> mock(LogRecordExporter.class));

    assertThat(builder.getLogRecordExporterCustomizers()).hasSize(2);
  }
}
