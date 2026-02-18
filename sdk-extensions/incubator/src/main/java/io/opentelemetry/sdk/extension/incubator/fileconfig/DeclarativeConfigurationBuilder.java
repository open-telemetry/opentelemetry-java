/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Builder for the declarative configuration. */
public class DeclarativeConfigurationBuilder implements DeclarativeConfigurationCustomizer {
  private Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel>
      modelCustomizer = Function.identity();

  private final List<ExporterCustomizer<SpanExporter>> spanExporterCustomizers = new ArrayList<>();
  private final List<ExporterCustomizer<MetricExporter>> metricExporterCustomizers =
      new ArrayList<>();
  private final List<ExporterCustomizer<LogRecordExporter>> logRecordExporterCustomizers =
      new ArrayList<>();

  static class ExporterCustomizer<T> {
    private final Class<? extends T> exporterType;
    private final BiFunction<T, DeclarativeConfigProperties, T> customizer;

    @SuppressWarnings("unchecked")
    <E extends T> ExporterCustomizer(
        Class<E> exporterType, BiFunction<E, DeclarativeConfigProperties, E> customizer) {
      this.exporterType = exporterType;
      this.customizer = (BiFunction<T, DeclarativeConfigProperties, T>) customizer;
    }

    Class<? extends T> getExporterType() {
      return exporterType;
    }

    BiFunction<T, DeclarativeConfigProperties, T> getCustomizer() {
      return customizer;
    }
  }

  @Override
  public void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer) {
    modelCustomizer = mergeCustomizer(modelCustomizer, customizer);
  }

  @Override
  public <T extends SpanExporter> void addSpanExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer) {
    spanExporterCustomizers.add(new ExporterCustomizer<>(exporterType, customizer));
  }

  @Override
  public <T extends MetricExporter> void addMetricExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer) {
    metricExporterCustomizers.add(new ExporterCustomizer<>(exporterType, customizer));
  }

  @Override
  public <T extends LogRecordExporter> void addLogRecordExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer) {
    logRecordExporterCustomizers.add(new ExporterCustomizer<>(exporterType, customizer));
  }

  private static <I, O1, O2> Function<I, O2> mergeCustomizer(
      Function<? super I, ? extends O1> first, Function<? super O1, ? extends O2> second) {
    return (I configured) -> {
      O1 firstResult = first.apply(configured);
      return second.apply(firstResult);
    };
  }

  /** Customize the configuration model. */
  public OpenTelemetryConfigurationModel customizeModel(
      OpenTelemetryConfigurationModel configurationModel) {
    return modelCustomizer.apply(configurationModel);
  }

  List<ExporterCustomizer<SpanExporter>> getSpanExporterCustomizers() {
    return spanExporterCustomizers;
  }

  List<ExporterCustomizer<MetricExporter>> getMetricExporterCustomizers() {
    return metricExporterCustomizers;
  }

  List<ExporterCustomizer<LogRecordExporter>> getLogRecordExporterCustomizers() {
    return logRecordExporterCustomizers;
  }
}
