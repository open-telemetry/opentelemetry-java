/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Builder for the declarative configuration. */
public class DeclarativeConfigurationBuilder implements DeclarativeConfigurationCustomizer {
  private Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel>
      modelCustomizer = Function.identity();

  private final List<Customizer<SpanExporter>> spanExporterCustomizers = new ArrayList<>();
  private final List<Customizer<MetricExporter>> metricExporterCustomizers = new ArrayList<>();
  private final List<Customizer<LogRecordExporter>> logRecordExporterCustomizers =
      new ArrayList<>();

  @Override
  public void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer) {
    modelCustomizer = mergeCustomizer(modelCustomizer, customizer);
  }

  @Override
  public <T extends SpanExporter> void addSpanExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer) {
    spanExporterCustomizers.add(new Customizer<>(exporterType, customizer));
  }

  @Override
  public <T extends MetricExporter> void addMetricExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer) {
    metricExporterCustomizers.add(new Customizer<>(exporterType, customizer));
  }

  @Override
  public <T extends LogRecordExporter> void addLogRecordExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer) {
    logRecordExporterCustomizers.add(new Customizer<>(exporterType, customizer));
  }

  List<Customizer<SpanExporter>> getSpanExporterCustomizers() {
    return Collections.unmodifiableList(spanExporterCustomizers);
  }

  List<Customizer<MetricExporter>> getMetricExporterCustomizers() {
    return Collections.unmodifiableList(metricExporterCustomizers);
  }

  List<Customizer<LogRecordExporter>> getLogRecordExporterCustomizers() {
    return Collections.unmodifiableList(logRecordExporterCustomizers);
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

  static class Customizer<T> {
    private static final Logger logger = Logger.getLogger(Customizer.class.getName());

    private final Class<? extends T> exporterType;
    private final BiFunction<T, DeclarativeConfigProperties, T> customizer;

    @SuppressWarnings("unchecked")
    <E extends T> Customizer(
        Class<E> exporterType, BiFunction<E, DeclarativeConfigProperties, E> customizer) {
      this.exporterType = exporterType;
      this.customizer = (BiFunction<T, DeclarativeConfigProperties, T>) customizer;
    }

    T maybeCustomize(T exporter, String name, DeclarativeConfigProperties properties) {
      if (!exporterType.isInstance(exporter)) {
        return exporter;
      }
      T customized = customizer.apply(exporter, properties);
      if (customized == null) {
        throw new DeclarativeConfigException(
            "Customizer returned null for " + exporterType.getSimpleName() + ": " + name);
      }
      if (customized != exporter && exporter instanceof Closeable) {
        try {
          ((Closeable) exporter).close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed to close exporter after customization", e);
        }
      }
      return customized;
    }
  }
}
