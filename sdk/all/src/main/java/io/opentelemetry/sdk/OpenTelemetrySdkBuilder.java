/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk.ObfuscatedTracerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.util.concurrent.atomic.AtomicBoolean;

/** A builder for configuring an {@link OpenTelemetrySdk}. */
public final class OpenTelemetrySdkBuilder {
  private static final AtomicBoolean INITIALIZED_GLOBAL = new AtomicBoolean();

  private ContextPropagators propagators = ContextPropagators.noop();
  private SdkTracerProvider tracerProvider;
  private SdkMeterProvider meterProvider;

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

  /**
   * Sets the {@link MeterProvider} to use.. This can be used to configure tracing settings by
   * returning the instance created by a {@link SdkMeterProvider.Builder}.
   *
   * @see SdkMeterProvider#builder()
   */
  @Deprecated
  public OpenTelemetrySdkBuilder setMeterProvider(SdkMeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  /** Sets the {@link ContextPropagators} to use. */
  public OpenTelemetrySdkBuilder setPropagators(ContextPropagators propagators) {
    this.propagators = propagators;
    return this;
  }

  /**
   * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link
   * OpenTelemetrySdkBuilder}.
   */
  public OpenTelemetrySdk build() {
    if (meterProvider == null) {
      meterProvider = SdkMeterProvider.builder().build();
    }

    if (tracerProvider == null) {
      tracerProvider = SdkTracerProvider.builder().build();
    }

    OpenTelemetrySdk sdk =
        new OpenTelemetrySdk(
            new ObfuscatedTracerProvider(tracerProvider), meterProvider, propagators);
    // Automatically initialize global OpenTelemetry with the first SDK we build.
    if (INITIALIZED_GLOBAL.compareAndSet(/* expectedValue= */ false, /* newValue= */ true)) {
      GlobalOpenTelemetry.set(sdk);
    }
    return sdk;
  }
}
