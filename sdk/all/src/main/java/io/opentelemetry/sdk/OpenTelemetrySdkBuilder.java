/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.internal.OpenTelemetrySdkBuilderUtil;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

/** A builder for configuring an {@link OpenTelemetrySdk}. */
public final class OpenTelemetrySdkBuilder {

  private ContextPropagators propagators = ContextPropagators.noop();
  @Nullable private SdkTracerProvider tracerProvider;
  @Nullable private SdkMeterProvider meterProvider;
  @Nullable private SdkLoggerProvider loggerProvider;
  @Nullable private Object configProvider;

  private static final boolean INCUBATOR_AVAILABLE;
  @Nullable private static final Method CREATE_EXTENDED_OPEN_TELEMETRY_SDK_METHOD;

  static {
    boolean incubatorAvailable = false;
    Method createExtendedOpenTelemetrySdk = null;
    try {
      Class.forName("io.opentelemetry.api.incubator.ExtendedOpenTelemetry");
      createExtendedOpenTelemetrySdk =
          Class.forName("io.opentelemetry.sdk.IncubatingUtil")
              .getDeclaredMethod(
                  "createExtendedOpenTelemetrySdk", OpenTelemetrySdk.class, Object.class);
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(
          "IncubatingUtil.createExtendedOpenTelemetrySdk could not be found."
              + " This is a bug in OpenTelemetry.",
          e);
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
    CREATE_EXTENDED_OPEN_TELEMETRY_SDK_METHOD = createExtendedOpenTelemetrySdk;
  }

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
   * @see SdkTracerProvider#builder()
   */
  public OpenTelemetrySdkBuilder setTracerProvider(SdkTracerProvider tracerProvider) {
    this.tracerProvider = tracerProvider;
    return this;
  }

  /**
   * Sets the {@link SdkMeterProvider} to use. This can be used to configure metric settings by
   * returning the instance created by a {@link
   * io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder}.
   *
   * @see SdkMeterProvider#builder()
   */
  public OpenTelemetrySdkBuilder setMeterProvider(SdkMeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  /**
   * Sets the {@link SdkLoggerProvider} to use. This can be used to configure log settings by
   * returning the instance created by a {@link SdkLoggerProviderBuilder}.
   *
   * @see SdkLoggerProvider#builder()
   * @since 1.19.0
   */
  public OpenTelemetrySdkBuilder setLoggerProvider(SdkLoggerProvider loggerProvider) {
    this.loggerProvider = loggerProvider;
    return this;
  }

  /** Sets the {@link ContextPropagators} to use. */
  public OpenTelemetrySdkBuilder setPropagators(ContextPropagators propagators) {
    this.propagators = requireNonNull(propagators);
    return this;
  }

  /**
   * Sets the SDK config provider to use.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * OpenTelemetrySdkBuilderUtil#setConfigProvider(OpenTelemetrySdkBuilder,
   * io.opentelemetry.sdk.internal.SdkConfigProvider)}.
   *
   * <p>The parameter type is {@link Object} to avoid introducing another direct incubator-linked
   * method signature in the path Groovy eagerly inspects.
   */
  OpenTelemetrySdkBuilder setConfigProvider(Object configProvider) {
    this.configProvider = requireNonNull(configProvider);
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
    SdkTracerProvider tracerProvider = this.tracerProvider;
    if (tracerProvider == null) {
      tracerProvider = SdkTracerProvider.builder().build();
    }

    SdkMeterProvider meterProvider = this.meterProvider;
    if (meterProvider == null) {
      meterProvider = SdkMeterProvider.builder().build();
    }

    SdkLoggerProvider loggerProvider = this.loggerProvider;
    if (loggerProvider == null) {
      loggerProvider = SdkLoggerProvider.builder().build();
    }

    OpenTelemetrySdk openTelemetrySdk =
        new OpenTelemetrySdk(tracerProvider, meterProvider, loggerProvider, propagators);
    return INCUBATOR_AVAILABLE
        ? createExtendedOpenTelemetrySdk(openTelemetrySdk, configProvider)
        : openTelemetrySdk;
  }

  private static OpenTelemetrySdk createExtendedOpenTelemetrySdk(
      OpenTelemetrySdk openTelemetrySdk, @Nullable Object configProvider) {
    return createExtendedOpenTelemetrySdk(
        openTelemetrySdk, configProvider, requireNonNull(CREATE_EXTENDED_OPEN_TELEMETRY_SDK_METHOD));
  }

  static OpenTelemetrySdk createExtendedOpenTelemetrySdk(
      OpenTelemetrySdk openTelemetrySdk,
      @Nullable Object configProvider,
      Method createExtendedOpenTelemetrySdkMethod) {
    try {
      return (OpenTelemetrySdk)
          createExtendedOpenTelemetrySdkMethod.invoke(null, openTelemetrySdk, configProvider);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(
          "IncubatingUtil.createExtendedOpenTelemetrySdk could not be invoked."
              + " This is a bug in OpenTelemetry.",
          e);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(
          "IncubatingUtil.createExtendedOpenTelemetrySdk could not be called with the expected"
              + " arguments. This is a bug in OpenTelemetry.",
          e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getTargetException();
      // Preserve the original application behavior rather than wrapping runtime failures emitted
      // by the incubator path in a reflective InvocationTargetException.
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw new IllegalStateException(
          "IncubatingUtil.createExtendedOpenTelemetrySdk failed."
              + " This is a bug in OpenTelemetry.",
          cause);
    }
  }
}
