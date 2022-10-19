/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.logs;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * A service provider interface (SPI) for providing additional exporters that can be used with the
 * autoconfigured SDK. If the {@code otel.logs.exporter} property contains a value equal to what is
 * returned by {@link #getName()}, the exporter returned by {@link
 * #createExporter(ConfigProperties)} will be enabled and added to the SDK.
 *
 * @since 1.19.0
 */
public interface ConfigurableLogRecordExporterProvider {

  /**
   * Returns a {@link LogRecordExporter} that can be registered to OpenTelemetry by providing the
   * property value specified by {@link #getName()}.
   */
  LogRecordExporter createExporter(ConfigProperties config);

  /**
   * Returns the name of this exporter, which can be specified with the {@code otel.logs.exporter}
   * property to enable it. The name returned should NOT be the same as any other exporter name. If
   * the name does conflict with another exporter name, the resulting behavior is undefined and it
   * is explicitly unspecified which exporter will actually be used.
   */
  String getName();
}
