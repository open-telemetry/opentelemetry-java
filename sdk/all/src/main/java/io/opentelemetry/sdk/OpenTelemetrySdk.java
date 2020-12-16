/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.DefaultOpenTelemetry;
import io.opentelemetry.api.DefaultOpenTelemetryBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.concurrent.ThreadSafe;

/** The SDK implementation of {@link OpenTelemetry}. */
@ThreadSafe
public final class OpenTelemetrySdk extends DefaultOpenTelemetry {

  /**
   * Returns a new {@link Builder} for configuring an instance of {@linkplain OpenTelemetrySdk the
   * OpenTelemetry SDK}.
   */
  public static Builder builder() {
    return new Builder();
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
          "Trying to access global TracerSdkManagement but global TracerProvider is not an "
              + "instance created by this SDK.");
    }
    return (SdkTracerProvider) ((ObfuscatedTracerProvider) tracerProvider).unobfuscate();
  }

  /**
   * Returns the global {@link MeterSdkProvider}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  public static MeterSdkProvider getGlobalMeterProvider() {
    return (MeterSdkProvider) GlobalOpenTelemetry.get().getMeterProvider();
  }

  private static final AtomicBoolean INITIALIZED_GLOBAL = new AtomicBoolean();

  private OpenTelemetrySdk(
      TracerProvider tracerProvider,
      MeterProvider meterProvider,
      ContextPropagators contextPropagators) {
    super(tracerProvider, meterProvider, contextPropagators);
  }

  /** Returns the {@link SdkTracerManagement} for this {@link OpenTelemetrySdk}. */
  public SdkTracerManagement getTracerManagement() {
    return (SdkTracerProvider) ((ObfuscatedTracerProvider) getTracerProvider()).unobfuscate();
  }

  /** A builder for configuring an {@link OpenTelemetrySdk}. */
  public static class Builder extends DefaultOpenTelemetryBuilder {
    /**
     * Sets the {@link SdkTracerProvider} to use. This can be used to configure tracing settings by
     * returning the instance created by a {@link SdkTracerProvider.Builder}.
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
    public Builder setTracerProvider(TracerProvider tracerProvider) {
      if (!(tracerProvider instanceof SdkTracerProvider)) {
        throw new IllegalArgumentException(
            "The OpenTelemetrySdk can only be configured with a TracerSdkProvider");
      }
      super.setTracerProvider(tracerProvider);
      return this;
    }

    /**
     * Sets the {@link MeterProvider} to use.. This can be used to configure tracing settings by
     * returning the instance created by a {@link MeterSdkProvider.Builder}.
     *
     * @see MeterSdkProvider#builder()
     */
    @Override
    @Deprecated
    public Builder setMeterProvider(MeterProvider meterProvider) {
      if (!(meterProvider instanceof MeterSdkProvider)) {
        throw new IllegalArgumentException(
            "The OpenTelemetrySdk can only be configured with a MeterSdkProvider");
      }
      super.setMeterProvider(meterProvider);
      return this;
    }

    /** Sets the {@link ContextPropagators} to use. */
    @Override
    public Builder setPropagators(ContextPropagators propagators) {
      super.setPropagators(propagators);
      return this;
    }

    /**
     * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link Builder}.
     */
    @Override
    public OpenTelemetrySdk build() {
      if (meterProvider == null) {
        meterProvider = MeterSdkProvider.builder().build();
      }

      if (tracerProvider == null) {
        tracerProvider = SdkTracerProvider.builder().build();
      }

      OpenTelemetrySdk sdk =
          new OpenTelemetrySdk(
              new ObfuscatedTracerProvider(tracerProvider), meterProvider, super.propagators);
      // Automatically initialize global OpenTelemetry with the first SDK we build.
      if (INITIALIZED_GLOBAL.compareAndSet(/* expectedValue= */ false, /* newValue= */ true)) {
        GlobalOpenTelemetry.set(sdk);
      }
      return sdk;
    }
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

    private final TracerProvider delegate;

    private ObfuscatedTracerProvider(TracerProvider delegate) {
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

    public TracerProvider unobfuscate() {
      return delegate;
    }
  }
}
