/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Builder for the declarative configuration. */
public class DeclarativeConfigurationBuilder implements DeclarativeConfigurationCustomizer {
  private Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel>
      modelCustomizer = Function.identity();

  private BiFunction<String, SpanExporter, SpanExporter> spanExporterCustomizer =
      (name, exporter) -> exporter;
  private BiFunction<String, MetricExporter, MetricExporter> metricExporterCustomizer =
      (name, exporter) -> exporter;
  private BiFunction<String, LogRecordExporter, LogRecordExporter> logRecordExporterCustomizer =
      (name, exporter) -> exporter;

  @Override
  public void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer) {
    modelCustomizer = mergeCustomizer(modelCustomizer, customizer);
  }

  @Override
  public void addSpanExporterCustomizer(BiFunction<String, SpanExporter, SpanExporter> customizer) {
    spanExporterCustomizer = mergeBiFunctionCustomizer(spanExporterCustomizer, customizer);
  }

  @Override
  public void addMetricExporterCustomizer(
      BiFunction<String, MetricExporter, MetricExporter> customizer) {
    metricExporterCustomizer = mergeBiFunctionCustomizer(metricExporterCustomizer, customizer);
  }

  @Override
  public void addLogRecordExporterCustomizer(
      BiFunction<String, LogRecordExporter, LogRecordExporter> customizer) {
    logRecordExporterCustomizer =
        mergeBiFunctionCustomizer(logRecordExporterCustomizer, customizer);
  }

  private static <I, O1, O2> Function<I, O2> mergeCustomizer(
      Function<? super I, ? extends O1> first, Function<? super O1, ? extends O2> second) {
    return (I configured) -> {
      O1 firstResult = first.apply(configured);
      return second.apply(firstResult);
    };
  }

  private static <K, V> BiFunction<K, V, V> mergeBiFunctionCustomizer(
      BiFunction<K, V, V> first, BiFunction<K, V, V> second) {
    return (K key, V value) -> {
      V firstResult = first.apply(key, value);
      return second.apply(key, firstResult);
    };
  }

  /** Customize the configuration model. */
  public OpenTelemetryConfigurationModel customizeModel(
      OpenTelemetryConfigurationModel configurationModel) {
    return modelCustomizer.apply(configurationModel);
  }

  BiFunction<String, SpanExporter, SpanExporter> getSpanExporterCustomizer() {
    return spanExporterCustomizer;
  }

  BiFunction<String, MetricExporter, MetricExporter> getMetricExporterCustomizer() {
    return metricExporterCustomizer;
  }

  BiFunction<String, LogRecordExporter, LogRecordExporter> getLogRecordExporterCustomizer() {
    return logRecordExporterCustomizer;
  }
}
