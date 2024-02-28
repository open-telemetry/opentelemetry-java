/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

/**
 * A service provider interface (SPI) for providing additional metric readers that can be used with
 * the autoconfigured SDK. If the {@code otel.metrics.exporter} property contains a value equal to
 * what is returned by {@link #getName()}, the exporter returned by {@link
 * #createMetricReader(ConfigProperties)} will be enabled and added to the SDK.
 *
 * <p>Where as {@link ConfigurableMetricExporterProvider} provides push-based {@link
 * MetricExporter}s to be paired with {@link PeriodicMetricReader}, this SPI facilitates pull-based
 * {@link MetricReader}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ConfigurableMetricReaderProvider {

  /**
   * Returns a {@link MetricReader} that can be registered to OpenTelemetry by providing the
   * property value specified by {@link #getName()}.
   */
  MetricReader createMetricReader(ConfigProperties config);

  /**
   * Returns the name of this reader, which can be specified with the {@code otel.metrics.exporter}
   * property to enable it. The name returned should NOT be the same as any other reader / exporter
   * name, either from other implementations of this SPI or {@link
   * ConfigurableMetricExporterProvider}. If the name does conflict with another reader / exporter
   * name, the resulting behavior is undefined and it is explicitly unspecified which reader /
   * exporter will actually be used.
   */
  String getName();
}
