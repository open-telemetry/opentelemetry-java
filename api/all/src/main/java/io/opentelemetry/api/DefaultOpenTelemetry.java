/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.spi.OpenTelemetryFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The default OpenTelemetry API, which tries to find API implementations via SPI or otherwise falls
 * back to no-op default implementations.
 */
@ThreadSafe
public class DefaultOpenTelemetry implements OpenTelemetry {
  private static final Object mutex = new Object();

  static OpenTelemetry getGlobalOpenTelemetry() {
    if (globalOpenTelemetry == null) {
      synchronized (mutex) {
        if (globalOpenTelemetry == null) {
          OpenTelemetryFactory openTelemetryFactory = Utils.loadSpi(OpenTelemetryFactory.class);
          if (openTelemetryFactory != null) {
            globalOpenTelemetry = openTelemetryFactory.create();
          } else {
            globalOpenTelemetry = builder().build();
          }
        }
      }
    }
    return globalOpenTelemetry;
  }

  static void setGlobalOpenTelemetry(OpenTelemetry openTelemetry) {
    globalOpenTelemetry = openTelemetry;
  }

  public static DefaultOpenTelemetryBuilder builder() {
    return new DefaultOpenTelemetryBuilder();
  }

  @Nullable private static volatile OpenTelemetry globalOpenTelemetry;

  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;

  private volatile ContextPropagators propagators;

  @Override
  public void setPropagators(ContextPropagators propagators) {
    this.propagators = propagators;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  public MeterProvider getMeterProvider() {
    return meterProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  protected DefaultOpenTelemetry(
      TracerProvider tracerProvider, MeterProvider meterProvider, ContextPropagators propagators) {
    this.tracerProvider = tracerProvider;
    this.meterProvider = meterProvider;
    this.propagators = propagators;
  }

  // for testing
  static void reset() {
    globalOpenTelemetry = null;
  }
}
