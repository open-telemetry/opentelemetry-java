/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;

/** An builder for creating an {@link ExtendedOpenTelemetrySdk} instance. */
public final class ExtendedOpenTelemetrySdkBuilder {
  private final SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder();
  private final SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
  private final SdkLoggerProviderBuilder loggerProviderBuilder = SdkLoggerProvider.builder();
  private ContextPropagators propagators = ContextPropagators.noop();
  private ConfigProvider configProvider =
      SdkConfigProvider.create(new OpenTelemetryConfigurationModel());
  private Consumer<Closeable> closeableConsumer =
      closeable -> {
        // Default no-op closeable consumer
      };

  /** Sets the {@link ContextPropagators} to use. */
  public ExtendedOpenTelemetrySdkBuilder setPropagators(ContextPropagators propagators) {
    this.propagators = Objects.requireNonNull(propagators, "propagators must not be null");
    return this;
  }

  /** Sets the {@link ConfigProvider} to use. */
  public ExtendedOpenTelemetrySdkBuilder setConfigProvider(ConfigProvider configProvider) {
    this.configProvider = Objects.requireNonNull(configProvider, "configProvider must not be null");
    return this;
  }

  public ExtendedOpenTelemetrySdkBuilder setCloseableConsumer(Consumer<Closeable> configurator) {
    this.closeableConsumer = Objects.requireNonNull(configurator, "configurator must not be null");
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
  public OpenTelemetrySdk build() {
    SdkTracerProvider tracerProvider = tracerProviderBuilder.build();
    SdkMeterProvider meterProvider = meterProviderBuilder.build();
    SdkLoggerProvider loggerProvider = loggerProviderBuilder.build();
    closeableConsumer.accept(tracerProvider);
    closeableConsumer.accept(meterProvider);
    closeableConsumer.accept(loggerProvider);
    ExtendedOpenTelemetrySdk extendedOpenTelemetrySdk =
        new ObfuscatedExtendedOpenTelemetrySdk(
            configProvider, tracerProvider, meterProvider, loggerProvider, propagators);
    closeableConsumer.accept(extendedOpenTelemetrySdk);

    OpenTelemetrySdkBuilder builder =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .setPropagators(propagators);

    try {
      Method setExtendedOpenTelemetrySdk =
          builder.getClass().getDeclaredMethod("setExtendedOpenTelemetrySdk", Object.class);
      setExtendedOpenTelemetrySdk.setAccessible(true);
      setExtendedOpenTelemetrySdk.invoke(builder, extendedOpenTelemetrySdk);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("unable to set extended OpenTelemetry SDK", e);
    }

    return builder.build();
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
}
