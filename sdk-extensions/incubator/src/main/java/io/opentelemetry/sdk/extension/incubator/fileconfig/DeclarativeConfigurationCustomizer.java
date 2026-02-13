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
   * @param customizer function receiving (exporterName, exporter) and returning customized exporter
   */
  void addSpanExporterCustomizer(BiFunction<String, SpanExporter, SpanExporter> customizer);

  /**
   * Add customizer for {@link MetricExporter} instances created from declarative configuration.
   * Multiple customizers compose in registration order.
   *
   * @param customizer function receiving (exporterName, exporter) and returning customized exporter
   */
  void addMetricExporterCustomizer(BiFunction<String, MetricExporter, MetricExporter> customizer);

  /**
   * Add customizer for {@link LogRecordExporter} instances created from declarative configuration.
   * Multiple customizers compose in registration order.
   *
   * <p>Important: Customizers must not return null. If the customizer wraps the exporter in a new
   * {@link java.io.Closeable} instance, the customizer is responsible for resource cleanup.
   *
   * @param customizer function receiving (exporterName, exporter) and returning customized exporter
   */
  void addLogRecordExporterCustomizer(
      BiFunction<String, LogRecordExporter, LogRecordExporter> customizer);
}
