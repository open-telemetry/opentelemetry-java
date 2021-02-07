/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

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
  private final ContextPropagators propagators;

  DefaultOpenTelemetry(TracerProvider tracerProvider, ContextPropagators propagators) {
    this.tracerProvider = tracerProvider;
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
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }
}
