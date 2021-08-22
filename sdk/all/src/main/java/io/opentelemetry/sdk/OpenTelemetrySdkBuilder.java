/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk.ObfuscatedTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import javax.annotation.Nullable;

/** A builder for configuring an {@link OpenTelemetrySdk}. */
public final class OpenTelemetrySdkBuilder {

  private ContextPropagators propagators = ContextPropagators.noop();
  @Nullable private SdkTracerProvider tracerProvider;

  /**
   * Package protected to disallow direct initialization.
   *
   * @see OpenTelemetrySdk#builder()
   */
  OpenTelemetrySdkBuilder() {}

  /**
   * Sets the {@link SdkTracerProvider} to use. This can be used to configure tracing settings by
   * returning the instance created by a {@link SdkTracerProviderBuilder}.
   *
   * <p>If you use this method, it is assumed that you are providing a fully configured
   * TracerSdkProvider, and other settings will be ignored.
   *
   * <p>Note: the parameter passed in here must be a {@link SdkTracerProvider} instance.
   *
   * @param tracerProvider A {@link SdkTracerProvider} to use with this instance.
   * @see SdkTracerProvider#builder()
   */
  public OpenTelemetrySdkBuilder setTracerProvider(SdkTracerProvider tracerProvider) {
    this.tracerProvider = tracerProvider;
    return this;
  }

  /** Sets the {@link ContextPropagators} to use. */
  public OpenTelemetrySdkBuilder setPropagators(ContextPropagators propagators) {
    this.propagators = propagators;
    return this;
  }

  /**
   * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link
   * OpenTelemetrySdkBuilder} and registers it as the global {@link
   * io.opentelemetry.api.OpenTelemetry}. An exception will be thrown if this method is attempted to
   * be called multiple times in the lifecycle of an application - ensure you have only one SDK for
   * use as the global instance. If you need to configure multiple SDKs for tests, use {@link
   * GlobalOpenTelemetry#resetForTest()} between them.
   *
   * @see GlobalOpenTelemetry
   */
  public OpenTelemetrySdk buildAndRegisterGlobal() {
    OpenTelemetrySdk sdk = build();
    GlobalOpenTelemetry.set(sdk);
    return sdk;
  }

  /**
   * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link
   * OpenTelemetrySdkBuilder}. This SDK is not registered as the global {@link
   * io.opentelemetry.api.OpenTelemetry}. It is recommended that you register one SDK using {@link
   * OpenTelemetrySdkBuilder#buildAndRegisterGlobal()} for use by instrumentation that requires
   * access to a global instance of {@link io.opentelemetry.api.OpenTelemetry}.
   *
   * @see GlobalOpenTelemetry
   */
  public OpenTelemetrySdk build() {
    if (tracerProvider == null) {
      tracerProvider = SdkTracerProvider.builder().build();
    }

    return new OpenTelemetrySdk(new ObfuscatedTracerProvider(tracerProvider), propagators);
  }
}
