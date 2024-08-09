/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.metrics.ExtendedDefaultMeterProvider;
import io.opentelemetry.api.incubator.trace.ExtendedDefaultTracerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The default OpenTelemetry API, which tries to find API implementations via SPI or otherwise falls
 * back to no-op default implementations.
 */
@ThreadSafe
final class ExtendedDefaultOpenTelemetry implements OpenTelemetry {
  private static final OpenTelemetry NO_OP =
      new ExtendedDefaultOpenTelemetry(ContextPropagators.noop());

  static OpenTelemetry getNoop() {
    return NO_OP;
  }

  static OpenTelemetry getPropagating(ContextPropagators propagators) {
    return new ExtendedDefaultOpenTelemetry(propagators);
  }

  private final ContextPropagators propagators;

  ExtendedDefaultOpenTelemetry(ContextPropagators propagators) {
    this.propagators = propagators;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return ExtendedDefaultTracerProvider.getInstance();
  }

  @Override
  public MeterProvider getMeterProvider() {
    return ExtendedDefaultMeterProvider.getInstance();
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  @Override
  public String toString() {
    return "DefaultOpenTelemetry{" + "propagators=" + propagators + "}";
  }
}
