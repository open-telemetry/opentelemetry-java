/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.metrics;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConfigurableMetricReaderProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;

/**
 * A service provider interface (SPI) for providing additional exporters that can be used with the
 * autoconfigured SDK. If the {@code otel.metrics.exporter} property contains a value equal to what
 * is returned by {@link #getName()}, the exporter returned by {@link
 * #createExporter(ConfigProperties)} will be enabled and added to the SDK.
 *
 * <p>The push-based {@link MetricExporter}s supplied by this SPI are paired with a {@link
 * io.opentelemetry.sdk.metrics.export.PeriodicMetricReader}. See {@link
 * ConfigurableMetricReaderProvider} for providing pull-based {@link MetricReader}s.
 *
 * @since 1.15.0
 */
public interface ConfigurableMetricExporterProvider {

  /**
   * Returns a {@link MetricExporter} that can be registered to OpenTelemetry by providing the
   * property value specified by {@link #getName()}.
   */
  MetricExporter createExporter(ConfigProperties config);

  /**
   * Returns the name of this exporter, which can be specified with the {@code
   * otel.metrics.exporter} property to enable it. The name returned should NOT be the same as any
   * other exporter / reader name, either from other implementations of this SPI or {@link
   * ConfigurableMetricReaderProvider}. If the name does conflict with another exporter / reader
   * name, the resulting behavior is undefined and it is explicitly unspecified which exporter /
   * reader will actually be used.
   */
  String getName();
}
