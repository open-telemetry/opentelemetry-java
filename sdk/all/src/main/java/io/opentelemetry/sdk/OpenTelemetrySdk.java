/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import javax.annotation.concurrent.ThreadSafe;

/** The SDK implementation of {@link OpenTelemetry}. */
@ThreadSafe
public final class OpenTelemetrySdk implements OpenTelemetry {
  private final ObfuscatedTracerProvider tracerProvider;
  private final ObfuscatedMeterProvider meterProvider;
  private final SdkLoggerProvider loggerProvider;
  private final ContextPropagators propagators;

  OpenTelemetrySdk(
      SdkTracerProvider tracerProvider,
      SdkMeterProvider meterProvider,
      SdkLoggerProvider loggerProvider,
      ContextPropagators propagators) {
    this.tracerProvider = new ObfuscatedTracerProvider(tracerProvider);
    this.meterProvider = new ObfuscatedMeterProvider(meterProvider);
    this.loggerProvider = loggerProvider;
    this.propagators = propagators;
  }

  /**
   * Returns a new {@link OpenTelemetrySdkBuilder} for configuring an instance of {@linkplain
   * OpenTelemetrySdk the OpenTelemetry SDK}.
   */
  public static OpenTelemetrySdkBuilder builder() {
    return new OpenTelemetrySdkBuilder();
  }

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  /** Returns the {@link SdkTracerProvider} for this {@link OpenTelemetrySdk}. */
  public SdkTracerProvider getSdkTracerProvider() {
    return tracerProvider.unobfuscate();
  }

  @Override
  public MeterProvider getMeterProvider() {
    return meterProvider;
  }

  /** Returns the {@link SdkMeterProvider} for this {@link OpenTelemetrySdk}. */
  public SdkMeterProvider getSdkMeterProvider() {
    return meterProvider.unobfuscate();
  }

  /**
   * Returns the {@link SdkLoggerProvider} for this {@link OpenTelemetrySdk}.
   *
   * @since 1.19.0
   */
  public SdkLoggerProvider getSdkLoggerProvider() {
    return loggerProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  @Override
  public String toString() {
    // TODO(anuraaga): Add logs / propagators
    return "OpenTelemetrySdk{"
        + "tracerProvider="
        + tracerProvider.unobfuscate()
        + ", meterProvider="
        + meterProvider.unobfuscate()
        + "}";
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated static global provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (SdkTracerProvider) openTelemetry.getTracerProvider()}.
   */
  @ThreadSafe
  // Visible for testing
  static class ObfuscatedTracerProvider implements TracerProvider {

    private final SdkTracerProvider delegate;

    ObfuscatedTracerProvider(SdkTracerProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public Tracer get(String instrumentationScopeName) {
      return delegate.get(instrumentationScopeName);
    }

    @Override
    public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
      return delegate.get(instrumentationScopeName, instrumentationScopeVersion);
    }

    @Override
    public TracerBuilder tracerBuilder(String instrumentationScopeName) {
      return delegate.tracerBuilder(instrumentationScopeName);
    }

    public SdkTracerProvider unobfuscate() {
      return delegate;
    }
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated static global provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (SdkMeterProvider) openTelemetry.getMeterProvider()}.
   */
  @ThreadSafe
  // Visible for testing
  static class ObfuscatedMeterProvider implements MeterProvider {

    private final SdkMeterProvider delegate;

    ObfuscatedMeterProvider(SdkMeterProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public MeterBuilder meterBuilder(String instrumentationScopeName) {
      return delegate.meterBuilder(instrumentationScopeName);
    }

    public SdkMeterProvider unobfuscate() {
      return delegate;
    }
  }
}
