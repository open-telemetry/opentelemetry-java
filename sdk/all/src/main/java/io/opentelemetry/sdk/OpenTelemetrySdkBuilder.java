/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.DefaultOpenTelemetryBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk.ObfuscatedTracerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

/** A builder for configuring an {@link OpenTelemetrySdk}. */
public final class OpenTelemetrySdkBuilder extends DefaultOpenTelemetryBuilder {
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
  @Override
  public OpenTelemetrySdkBuilder setTracerProvider(TracerProvider tracerProvider) {
    if (!(tracerProvider instanceof SdkTracerProvider)) {
      throw new IllegalArgumentException(
          "The OpenTelemetrySdk can only be configured with a TracerSdkProvider");
    }
    super.setTracerProvider(tracerProvider);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use.. This can be used to configure tracing settings by
   * returning the instance created by a {@link SdkMeterProvider.Builder}.
   *
   * @see SdkMeterProvider#builder()
   */
  @Override
  @Deprecated
  public OpenTelemetrySdkBuilder setMeterProvider(MeterProvider meterProvider) {
    if (!(meterProvider instanceof SdkMeterProvider)) {
      throw new IllegalArgumentException(
          "The OpenTelemetrySdk can only be configured with a MeterSdkProvider");
    }
    super.setMeterProvider(meterProvider);
    return this;
  }

  /** Sets the {@link ContextPropagators} to use. */
  @Override
  public OpenTelemetrySdkBuilder setPropagators(ContextPropagators propagators) {
    super.setPropagators(propagators);
    return this;
  }

  /**
   * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link
   * OpenTelemetrySdkBuilder}.
   */
  @Override
  public OpenTelemetrySdk build() {
    if (meterProvider == null) {
      meterProvider = SdkMeterProvider.builder().build();
    }

    if (tracerProvider == null) {
      tracerProvider = SdkTracerProvider.builder().build();
    }

    OpenTelemetrySdk sdk =
        new OpenTelemetrySdk(
            new ObfuscatedTracerProvider(tracerProvider), meterProvider, super.propagators);
    // Automatically initialize global OpenTelemetry with the first SDK we build.
    if (OpenTelemetrySdk.INITIALIZED_GLOBAL.compareAndSet(
        /* expectedValue= */ false, /* newValue= */ true)) {
      GlobalOpenTelemetry.set(sdk);
    }
    return sdk;
  }
}
