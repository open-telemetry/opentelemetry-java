/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
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

  /** Returns the global {@link OpenTelemetrySdk}. */
  public static OpenTelemetrySdk get() {
    return (OpenTelemetrySdk) GlobalOpenTelemetry.get();
  }

  /** Returns the global {@link SdkTracerManagement}. */
  public static SdkTracerManagement getGlobalTracerManagement() {
    TracerProvider tracerProvider = GlobalOpenTelemetry.get().getTracerProvider();
    if (!(tracerProvider instanceof ObfuscatedTracerProvider)) {
      throw new IllegalStateException(
          "Trying to access global SdkTracerManagement but global TracerProvider is not an "
              + "instance created by this SDK.");
    }
    return ((ObfuscatedTracerProvider) tracerProvider).unobfuscate();
  }

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  /** Returns the {@link SdkTracerManagement} for this {@link OpenTelemetrySdk}. */
  public SdkTracerManagement getTracerManagement() {
    return tracerProvider.unobfuscate();
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
