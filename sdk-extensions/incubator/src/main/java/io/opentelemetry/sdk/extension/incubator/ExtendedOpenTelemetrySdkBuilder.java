/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.incubator.entities.ResourceProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.extension.incubator.entities.SdkResourceProvider;
import io.opentelemetry.sdk.extension.incubator.entities.SdkResourceProviderBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import java.util.function.Consumer;

/** A new interface for creating OpenTelemetrySdk that supports {@link ResourceProvider}. */
public final class ExtendedOpenTelemetrySdkBuilder {
  private ContextPropagators propagators = ContextPropagators.noop();
  private final SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder();
  private final SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
  private final SdkLoggerProviderBuilder loggerProviderBuilder = SdkLoggerProvider.builder();
  private final SdkResourceProviderBuilder resourceProviderBuilder = SdkResourceProvider.builder();

  /** Sets the {@link ContextPropagators} to use. */
  public ExtendedOpenTelemetrySdkBuilder setPropagators(ContextPropagators propagators) {
    this.propagators = propagators;
    return this;
  }

  /**
   * Applies a consumer callback to configure the TracerProvider being built for this OpenTelemetry.
   *
   * @param configurator A callback fleshing out tracers.
   * @return this
   */
  public ExtendedOpenTelemetrySdkBuilder withTracerProvider(
      Consumer<SdkTracerProviderBuilder> configurator) {
    configurator.accept(this.tracerProviderBuilder);
    return this;
  }

  /**
   * Applies a consumer callback to configure the MeterProvider being built for this OpenTelemetry.
   *
   * @param configurator A callback fleshing out meters.
   * @return this
   */
  public ExtendedOpenTelemetrySdkBuilder withMeterProvider(
      Consumer<SdkMeterProviderBuilder> configurator) {
    configurator.accept(this.meterProviderBuilder);
    return this;
  }

  /**
   * Applies a consumer callback to configure the LoggerProvider being built for this OpenTelemetry.
   *
   * @param configurator A callback fleshing out meters.
   * @return this
   */
  public ExtendedOpenTelemetrySdkBuilder withLoggerProvider(
      Consumer<SdkLoggerProviderBuilder> configurator) {
    configurator.accept(this.loggerProviderBuilder);
    return this;
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
  public ExtendedOpenTelemetrySdk build() {
    SdkResourceProvider resourceProvider = resourceProviderBuilder.build();
    SdkTracerProvider tracerProvider =
        SdkTracerProviderUtil.setResourceSupplier(
                tracerProviderBuilder, resourceProvider::getSdkResource)
            .build();
    SdkMeterProvider meterProvider =
        SdkMeterProviderUtil.setResourceSupplier(
                meterProviderBuilder, resourceProvider::getSdkResource)
            .build();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProviderUtil.setResourceSupplier(
                loggerProviderBuilder, resourceProvider::getSdkResource)
            .build();
    return new ObfuscatedExtendedOpenTelemerySdk(
        resourceProvider, tracerProvider, meterProvider, loggerProvider, propagators);
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
  public ExtendedOpenTelemetrySdk buildAndRegisterGlobal() {
    ExtendedOpenTelemetrySdk sdk = build();
    GlobalOpenTelemetry.set(sdk);
    return sdk;
  }
}
