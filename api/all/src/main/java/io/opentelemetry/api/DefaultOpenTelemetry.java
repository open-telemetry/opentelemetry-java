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
  private static final OpenTelemetry NO_OP = new DefaultOpenTelemetry(ContextPropagators.noop());

  static OpenTelemetry noop() {
    return NO_OP;
  }

  static OpenTelemetry propagating(ContextPropagators propagators) {
    return new DefaultOpenTelemetry(propagators);
  }

  private final ContextPropagators propagators;

  DefaultOpenTelemetry(ContextPropagators propagators) {
    this.propagators = propagators;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return TracerProvider.getDefault();
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }
}
