/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.internal;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Static global instances are obfuscated when they are returned from the API to prevent users from
 * casting them to their SDK-specific implementation. For example, we do not want users to use
 * patterns like {@code (OpenTelemetrySdk) GlobalOpenTelemetry.get()}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
public final class ObfuscatedExtendedOpenTelemetry implements ExtendedOpenTelemetry {

  private final ExtendedOpenTelemetry delegate;

  /**
   * This constructor is called via reflection from {@link
   * io.opentelemetry.api.internal.IncubatingUtil#obfuscatedOpenTelemetryIfIncubating(OpenTelemetry)}.
   */
  public ObfuscatedExtendedOpenTelemetry(ExtendedOpenTelemetry delegate) {
    this.delegate = delegate;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return delegate.getTracerProvider();
  }

  @Override
  public MeterProvider getMeterProvider() {
    return delegate.getMeterProvider();
  }

  @Override
  public LoggerProvider getLogsBridge() {
    return delegate.getLogsBridge();
  }

  @Override
  public ContextPropagators getPropagators() {
    return delegate.getPropagators();
  }

  @Override
  public TracerBuilder tracerBuilder(String instrumentationScopeName) {
    return delegate.tracerBuilder(instrumentationScopeName);
  }

  @Override
  public ConfigProvider getConfigProvider() {
    return delegate.getConfigProvider();
  }
}
