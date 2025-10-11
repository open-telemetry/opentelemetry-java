/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/**
 * Provides configured instances of SDK extension components. {@link ComponentProvider} allows SDK
 * extension components which are not part of the core SDK to be referenced in declarative based
 * configuration.
 *
 * <p>NOTE: when {@link #getType()} is {@link Resource}, the {@link #getName()} is not (currently)
 * used, and {@link #create(DeclarativeConfigProperties)} is (currently) called with an empty {@link
 * DeclarativeConfigProperties}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>Supported component types include: {@link SpanExporter}, {@link MetricExporter}, {@link
 * LogRecordExporter}, {@link SpanProcessor}, {@link LogRecordProcessor}, {@link TextMapPropagator},
 * {@link Sampler}, {@link Resource}.
 */
public interface ComponentProvider {

  /**
   * The type of SDK extension component. For example, if providing instances of a custom span
   * exporter, the type would be {@link SpanExporter}.
   */
  Class<?> getType();

  /**
   * The name of the exporter, to be referenced in configuration files. For example, if providing
   * instances of a custom span exporter for the "acme" protocol, the name might be "acme".
   *
   * <p>This name MUST not be the same as any other component provider name which returns components
   * of the same {@link #getType() type}. In other words, {@link #getType()} and name form a
   * composite key uniquely identifying the provider.
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
  Object create(DeclarativeConfigProperties config);
}
