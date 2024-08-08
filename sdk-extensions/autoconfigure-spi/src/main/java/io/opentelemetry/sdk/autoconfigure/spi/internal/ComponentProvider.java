/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Provides configured instances of SDK extension components. {@link ComponentProvider} allows SDK
 * extension components which are not part of the core SDK to be referenced in file based
 * configuration.
 *
 * @param <T> the type of the SDK extension component. See {@link #getType()}. Supported values
 *     include: {@link SpanExporter}, {@link MetricExporter}, {@link LogRecordExporter}.
 */
// TODO: add support for Sampler, LogRecordProcessor, SpanProcessor, MetricReader
public interface ComponentProvider<T> {

  /**
   * The type of SDK extension component. For example, if providing instances of a custom span
   * exporter, the type would be {@link SpanExporter}.
   */
  Class<T> getType();

  /**
   * The name of the exporter, to be referenced in configuration files. For example, if providing
   * instances of a custom span exporter for the "acme" protocol, the name might be "acme".
   *
   * <p>This name MUST not be the same as any other component provider name which returns components
   * of the same {@link #getType() type}.
   */
  String getName();

  /**
   * Configure an instance of the SDK extension component according to the {@code config}.
   *
   * @param config the configuration provided where the component is referenced in a configuration
   *     file.
   * @return an instance the SDK extension component
   */
  // TODO (jack-berg): consider dynamic configuration use case before stabilizing in case that
  // affects any API decisions
  T create(StructuredConfigProperties config);
}
