/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The default OpenTelemetry API, which tries to find API implementations via SPI or otherwise falls
 * back to no-op default implementations.
 */
@ThreadSafe
public final class DefaultOpenTelemetry implements OpenTelemetry {
  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;
  private volatile ContextPropagators propagators;

  DefaultOpenTelemetry(
      TracerProvider tracerProvider, MeterProvider meterProvider, ContextPropagators propagators) {
    this.tracerProvider = tracerProvider;
    this.meterProvider = meterProvider;
    this.propagators = propagators;
  }

  /**
   * Returns a builder for the {@link DefaultOpenTelemetry}.
   *
   * @return a builder for the {@link DefaultOpenTelemetry}.
   */
  public static DefaultOpenTelemetryBuilder builder() {
    return new DefaultOpenTelemetryBuilder();
  }

  @Override
  @Deprecated
  public void setPropagators(ContextPropagators propagators) {
    this.propagators = propagators;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  @Deprecated
  public MeterProvider getMeterProvider() {
    return meterProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }
}
