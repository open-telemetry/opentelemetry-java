/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import javax.annotation.concurrent.ThreadSafe;

/** The SDK implementation of {@link OpenTelemetry}. */
@ThreadSafe
public final class OpenTelemetrySdk implements OpenTelemetry {
  private final ObfuscatedTracerProvider tracerProvider;
  private final ContextPropagators propagators;

  OpenTelemetrySdk(ObfuscatedTracerProvider tracerProvider, ContextPropagators propagators) {
    this.tracerProvider = tracerProvider;
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
  public ContextPropagators getPropagators() {
    return propagators;
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated static global provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (TracerSdkProvider) OpenTelemetry.getGlobalTracerProvider()}.
   */
  @ThreadSafe
  // Visible for testing
  static class ObfuscatedTracerProvider implements TracerProvider {

    private final SdkTracerProvider delegate;

    ObfuscatedTracerProvider(SdkTracerProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public Tracer get(String instrumentationName) {
      return delegate.get(instrumentationName);
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return delegate.get(instrumentationName, instrumentationVersion);
    }

    public SdkTracerProvider unobfuscate() {
      return delegate;
    }
  }
}
