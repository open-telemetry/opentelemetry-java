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
import java.util.function.BiFunction;
import java.util.function.Function;

/** A service provider interface (SPI) for customizing declarative configuration. */
public interface DeclarativeConfigurationCustomizer {
  /**
   * Method invoked when configuring the SDK to allow further customization of the declarative
   * configuration.
   *
   * @param customizer the customizer to add
   */
  void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer);

  /**
   * Add customizer for {@link SpanExporter} instances created from declarative configuration.
   * Multiple customizers compose in registration order.
   *
   * @param exporterType the exporter type to customize
   * @param customizer function receiving (exporter, properties) and returning customized exporter;
   *     must not return null
   * @param <T> the exporter type
   */
  <T extends SpanExporter> void addSpanExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer);

  /**
   * Add customizer for {@link MetricExporter} instances created from declarative configuration.
   * Multiple customizers compose in registration order.
   *
   * @param exporterType the exporter type to customize
   * @param customizer function receiving (exporter, properties) and returning customized exporter;
   *     must not return null
   * @param <T> the exporter type
   */
  <T extends MetricExporter> void addMetricExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer);

  /**
   * Add customizer for {@link LogRecordExporter} instances created from declarative configuration.
   * Multiple customizers compose in registration order.
   *
   * <p>If the customizer wraps the exporter in a new {@link java.io.Closeable} instance, the
   * customizer is responsible for resource cleanup.
   *
   * @param exporterType the exporter type to customize
   * @param customizer function receiving (exporter, properties) and returning customized exporter;
   *     must not return null
   * @param <T> the exporter type
   */
  <T extends LogRecordExporter> void addLogRecordExporterCustomizer(
      Class<T> exporterType, BiFunction<T, DeclarativeConfigProperties, T> customizer);
}
