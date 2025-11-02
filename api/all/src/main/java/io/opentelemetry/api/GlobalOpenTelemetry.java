/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.internal.IncubatingUtil;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A global singleton for the entrypoint to telemetry functionality for tracing, metrics and
 * baggage.
 *
 * <p>If using the OpenTelemetry SDK, you may want to instantiate the {@link OpenTelemetry} to
 * provide configuration, for example of {@code Resource} or {@code Sampler}. See {@code
 * OpenTelemetrySdk} and {@code OpenTelemetrySdk.builder} for information on how to construct the
 * SDK's {@link OpenTelemetry} implementation.
 *
 * <p>WARNING: Due to the inherent complications around initialization order involving this class
 * and its single global instance, we strongly recommend *not* using GlobalOpenTelemetry unless you
 * have a use-case that absolutely requires it. Please favor using instances of OpenTelemetry
 * wherever possible.
 *
 * <p>If you are using the OpenTelemetry javaagent, it is generally best to only call
 * GlobalOpenTelemetry.get() once, and then pass the resulting reference where you need to use it.
 *
 * @see TracerProvider
 * @see ContextPropagators
 */
// We intentionally assign to be use for error reporting.
@SuppressWarnings("StaticAssignmentOfThrowable")
public final class GlobalOpenTelemetry {

  private static final String GLOBAL_AUTOCONFIGURE_ENABLED_PROPERTY =
      "otel.java.global-autoconfigure.enabled";

  private static final Logger logger = Logger.getLogger(GlobalOpenTelemetry.class.getName());

  private static final Object mutex = new Object();

  @SuppressWarnings("NonFinalStaticField")
  @Nullable
  private static volatile OpenTelemetry globalOpenTelemetry;

  @SuppressWarnings("NonFinalStaticField")
  @GuardedBy("mutex")
  @Nullable
  private static Throwable setGlobalCaller;

  private GlobalOpenTelemetry() {}

  /**
   * Returns the registered global {@link OpenTelemetry}.
   *
   * @throws IllegalStateException if a provider has been specified by system property using the
   *     interface FQCN but the specified provider cannot be found.
   */
  public static OpenTelemetry get() {
    OpenTelemetry openTelemetry = globalOpenTelemetry;
    if (openTelemetry == null) {
      synchronized (mutex) {
        openTelemetry = globalOpenTelemetry;
        if (openTelemetry == null) {

          OpenTelemetry autoConfigured = maybeAutoConfigureAndSetGlobal();
          if (autoConfigured != null) {
            return autoConfigured;
          }

          set(OpenTelemetry.noop());
          return OpenTelemetry.noop();
        }
      }
    }
    return openTelemetry;
  }

  /**
   * Sets the {@link OpenTelemetry} that should be the global instance. Future calls to {@link
   * #get()} will return the provided {@link OpenTelemetry} instance. This should be called once as
   * early as possible in your application initialization logic, often in a {@code static} block in
   * your main class. It should only be called once - an attempt to call it a second time will
   * result in an error. If trying to set the global {@link OpenTelemetry} multiple times in tests,
   * use {@link GlobalOpenTelemetry#resetForTest()} between them.
   *
   * <p>If you are using the OpenTelemetry SDK, you should generally use {@code
   * OpenTelemetrySdk.builder().buildAndRegisterGlobal()} instead of calling this method directly.
   */
  public static void set(OpenTelemetry openTelemetry) {
    synchronized (mutex) {
      if (globalOpenTelemetry != null) {
        throw new IllegalStateException(
            "GlobalOpenTelemetry.set has already been called. GlobalOpenTelemetry.set must be "
                + "called only once before any calls to GlobalOpenTelemetry.get. If you are using "
                + "the OpenTelemetrySdk, use OpenTelemetrySdkBuilder.buildAndRegisterGlobal "
                + "instead. Previous invocation set to cause of this exception.",
            setGlobalCaller);
      }
      globalOpenTelemetry = obfuscatedOpenTelemetry(openTelemetry);
      setGlobalCaller = new Throwable();
    }
  }

  /**
   * Sets the {@link OpenTelemetry} that should be the global instance.
   *
   * <p>This method calls the given {@code supplier} and calls {@link #set(OpenTelemetry)}, all
   * while holding the {@link GlobalOpenTelemetry} mutex.
   *
   * @since 1.52.0
   */
  public static void set(Supplier<OpenTelemetry> supplier) {
    synchronized (mutex) {
      OpenTelemetry openTelemetry = supplier.get();
      set(openTelemetry);
    }
  }

  /** Returns the globally registered {@link TracerProvider}. */
  public static TracerProvider getTracerProvider() {
    return get().getTracerProvider();
  }

  /**
   * Gets or creates a named tracer instance from the globally registered {@link TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationScopeName)}
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a tracer instance.
   */
  public static Tracer getTracer(String instrumentationScopeName) {
    return get().getTracer(instrumentationScopeName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the globally registered {@link
   * TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationScopeName,
   * instrumentationScopeVersion)}
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @param instrumentationScopeVersion The version of the instrumentation scope (e.g., "1.0.0").
   * @return a tracer instance.
   */
  public static Tracer getTracer(
      String instrumentationScopeName, String instrumentationScopeVersion) {
    return get().getTracer(instrumentationScopeName, instrumentationScopeVersion);
  }

  /**
   * Creates a TracerBuilder for a named {@link Tracer} instance.
   *
   * <p>This is a shortcut method for {@code get().tracerBuilder(instrumentationScopeName)}
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a TracerBuilder instance.
   * @since 1.4.0
   */
  public static TracerBuilder tracerBuilder(String instrumentationScopeName) {
    return get().tracerBuilder(instrumentationScopeName);
  }

  /**
   * Returns the globally registered {@link MeterProvider}.
   *
   * @since 1.10.0
   */
  public static MeterProvider getMeterProvider() {
    return get().getMeterProvider();
  }

  /**
   * Gets or creates a named meter instance from the globally registered {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationScopeName)}
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Meter instance.
   * @since 1.10.0
   */
  public static Meter getMeter(String instrumentationScopeName) {
    return get().getMeter(instrumentationScopeName);
  }

  /**
   * Creates a MeterBuilder for a named {@link Meter} instance.
   *
   * <p>This is a shortcut method for {@code get().meterBuilder(instrumentationScopeName)}
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a MeterBuilder instance.
   * @since 1.10.0
   */
  public static MeterBuilder meterBuilder(String instrumentationScopeName) {
    return get().meterBuilder(instrumentationScopeName);
  }

  /**
   * Unsets the global {@link OpenTelemetry}. This is only meant to be used from tests which need to
   * reconfigure {@link OpenTelemetry}.
   */
  public static void resetForTest() {
    globalOpenTelemetry = null;
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  public static ContextPropagators getPropagators() {
    return get().getPropagators();
  }

  @Nullable
  private static OpenTelemetry maybeAutoConfigureAndSetGlobal() {
    Class<?> openTelemetrySdkAutoConfiguration;
    try {
      openTelemetrySdkAutoConfiguration =
          Class.forName("io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk");
    } catch (ClassNotFoundException e) {
      return null;
    }

    // If autoconfigure module is present but global autoconfigure disabled log a warning and return
    boolean globalAutoconfigureEnabled =
        Boolean.parseBoolean(ConfigUtil.getString(GLOBAL_AUTOCONFIGURE_ENABLED_PROPERTY, "false"));
    if (!globalAutoconfigureEnabled) {
      logger.log(
          Level.INFO,
          "AutoConfiguredOpenTelemetrySdk found on classpath but automatic configuration is disabled."
              + " To enable, run your JVM with -D"
              + GLOBAL_AUTOCONFIGURE_ENABLED_PROPERTY
              + "=true");
      return null;
    }

    try {
      Method initialize = openTelemetrySdkAutoConfiguration.getMethod("initialize");
      Object autoConfiguredSdk = initialize.invoke(null);
      Method getOpenTelemetrySdk =
          openTelemetrySdkAutoConfiguration.getMethod("getOpenTelemetrySdk");
      return obfuscatedOpenTelemetry((OpenTelemetry) getOpenTelemetrySdk.invoke(autoConfiguredSdk));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(
          "AutoConfiguredOpenTelemetrySdk detected on classpath "
              + "but could not invoke initialize method. This is a bug in OpenTelemetry.",
          e);
    } catch (InvocationTargetException t) {
      logger.log(
          Level.SEVERE,
          "Error automatically configuring OpenTelemetry SDK. OpenTelemetry will not be enabled.",
          t.getTargetException());
      return null;
    }
  }

  private static OpenTelemetry obfuscatedOpenTelemetry(OpenTelemetry openTelemetry) {
    OpenTelemetry incubating = IncubatingUtil.obfuscatedOpenTelemetryIfIncubating(openTelemetry);
    if (incubating != null) {
      return incubating;
    }
    return new ObfuscatedOpenTelemetry(openTelemetry);
  }

  /**
   * Static global instances are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK-specific implementation. For example, we do not want users to
   * use patterns like {@code (OpenTelemetrySdk) GlobalOpenTelemetry.get()}.
   */
  @ThreadSafe
  static class ObfuscatedOpenTelemetry implements OpenTelemetry {

    private final OpenTelemetry delegate;

    ObfuscatedOpenTelemetry(OpenTelemetry delegate) {
      this.delegate = delegate;
    }

    @Override
    public TracerProvider getTracerProvider() {
      return delegate.getTracerProvider();
    }

    @Override
    public MeterProvider getMeterProvider() {
      return delegate.getMeterProvider();
    }

    @Override
    public LoggerProvider getLogsBridge() {
      return delegate.getLogsBridge();
    }

    @Override
    public ContextPropagators getPropagators() {
      return delegate.getPropagators();
    }

    @Override
    public TracerBuilder tracerBuilder(String instrumentationScopeName) {
      return delegate.tracerBuilder(instrumentationScopeName);
    }
  }
}
