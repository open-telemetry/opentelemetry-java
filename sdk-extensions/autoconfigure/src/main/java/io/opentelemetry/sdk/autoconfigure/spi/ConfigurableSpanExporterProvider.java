/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.sdk.autoconfigure.ConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * A service provider interface (SPI) for providing additional exporters that can be used with the
 * autoconfigured SDK. If the {@code otel.trace.exporter} property contains a value equal to what is
 * returned by {@link #getName()}, the exporter returned by {@link
 * #createExporter(ConfigProperties)} will be enabled and added to the SDK.
 */
public interface ConfigurableSpanExporterProvider {

  /**
   * Returns a {@link SpanExporter} that can be registered to OpenTelemetry by providing the
   * property value specified by {@link #getName()}.
   */
  SpanExporter createExporter(ConfigProperties config);

  /**
   * Returns the name of this exporter, which can be specified with the {@code otel.trace.exporter}
   * property to enable it. If the name is the same as any other defined exporter name, it is
   * undefined which will be used.
   */
  String getName();
}
