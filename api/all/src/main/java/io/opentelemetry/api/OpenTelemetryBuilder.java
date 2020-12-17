/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;

/**
 * A builder of an implementation of the OpenTelemetry API. Generally used to reconfigure SDK
 * implementations.
 *
 * @deprecated use the {@link DefaultOpenTelemetryBuilder} instead.
 */
@Deprecated
public interface OpenTelemetryBuilder<T extends OpenTelemetryBuilder<T>> {

  /** Sets the {@link TracerProvider} to use. */
  T setTracerProvider(TracerProvider tracerProvider);

  /**
   * Sets the {@link MeterProvider} to use.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  T setMeterProvider(MeterProvider meterProvider);

  /** Sets the {@link ContextPropagators} to use. */
  T setPropagators(ContextPropagators propagators);

  /**
   * Returns a new {@link OpenTelemetry} based on the configuration in this {@link
   * OpenTelemetryBuilder}.
   */
  OpenTelemetry build();
}
