/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A global singleton for the entrypoint to telemetry functionality for tracing, metrics and
 * baggage.
 *
 * <p>If using the OpenTelemetry SDK, you may want to instantiate the {@link OpenTelemetry} to
 * provide configuration, for example of {@code Resource} or {@code Sampler}. See {@code
 * OpenTelemetrySdk} and {@code OpenTelemetrySdk.builder} for information on how to construct the
 * SDK {@link OpenTelemetry}.
 *
 * @see TracerProvider
 * @see ContextPropagators
 */
@SuppressWarnings("deprecation") // Remove after deleting OpenTelemetry SPI
public final class GlobalOpenTelemetry {

  private static final OpenTelemetry NOOP = DefaultOpenTelemetry.builder().build();

  private static final boolean suppressSdkCheck =
      Boolean.getBoolean("otel.sdk.suppress-sdk-initialized-warning");

  private static final Logger logger = Logger.getLogger(GlobalOpenTelemetry.class.getName());

  private static final Object mutex = new Object();

  @Nullable private static volatile OpenTelemetry globalOpenTelemetry;

  private GlobalOpenTelemetry() {}

  /**
   * Returns the registered global {@link OpenTelemetry}.
   *
   * @throws IllegalStateException if a provider has been specified by system property using the
   *     interface FQCN but the specified provider cannot be found.
   */
  public static OpenTelemetry get() {
    OpenTelemetry current = globalOpenTelemetry;
    if (current == null) {
      synchronized (mutex) {
        if (globalOpenTelemetry == null) {
          OpenTelemetry autoConfigured = maybeAutoConfigure();
          if (autoConfigured != null) {
            set(autoConfigured);
            return autoConfigured;
          }

          if (!suppressSdkCheck) {
            SdkChecker.logIfSdkFound();
          }

          return NOOP;
        }
      }
    }
    return current;
  }

  /**
   * Sets the {@link OpenTelemetry} that should be the global instance. Future calls to {@link
   * #get()} will return the provided {@link OpenTelemetry} instance. This should be called once as
   * early as possible in your application initialization logic, often in a {@code static} block in
   * your main class.
   */
  public static void set(OpenTelemetry openTelemetry) {
    globalOpenTelemetry = openTelemetry;
  }

  // for testing
  static void reset() {
    globalOpenTelemetry = null;
  }

  /** Returns the globally registered {@link TracerProvider}. */
  public static TracerProvider getTracerProvider() {
    return get().getTracerProvider();
  }

  /**
   * Gets or creates a named tracer instance from the globally registered {@link TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationName)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  public static Tracer getTracer(String instrumentationName) {
    return get().getTracer(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the globally registered {@link
   * TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationName,
   * instrumentationVersion)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g., "1.0.0").
   * @return a tracer instance.
   */
  public static Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return get().getTracer(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  public static ContextPropagators getPropagators() {
    return get().getPropagators();
  }

  // Use an inner class that checks and logs in its static initializer to have log-once behavior
  // using initial classloader lock and no further runtime locks or atomics.
  private static class SdkChecker {
    static {
      boolean hasSdk = false;
      try {
        Class.forName("io.opentelemetry.sdk.OpenTelemetrySdk");
        hasSdk = true;
      } catch (Throwable t) {
        // Ignore
      }

      if (hasSdk) {
        logger.log(
            Level.SEVERE,
            "Attempt to access GlobalOpenTelemetry.get before OpenTelemetrySdk has been "
                + "initialized. This generally means telemetry will not be recorded for parts of "
                + "your application. Make sure to initialize OpenTelemetrySdk, using "
                + "OpenTelemetrySdk.builder()...buildAndRegisterGlobal(), as early as possible in "
                + "your application.  If you do not need to use the OpenTelemetry SDK, either "
                + "exclude it from your classpath or set the "
                + "'otel.sdk.suppress-sdk-initialized-warning' system property to true.",
            // Add stack trace to log to allow user to find the problematic invocation.
            new Throwable());
      }
    }

    // All the logic is in the static initializer, this method is called just to load the class and
    // that's it. JVM will then optimize it away completely because it's empty so we have no
    // overhead for a log-once pattern.
    static void logIfSdkFound() {}
  }

  @Nullable
  private static OpenTelemetry maybeAutoConfigure() {
    final Class<?> openTelemetrySdkAutoConfiguration;
    try {
      openTelemetrySdkAutoConfiguration =
          Class.forName("io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration");
    } catch (ClassNotFoundException e) {
      return null;
    }

    try {
      Method initialize = openTelemetrySdkAutoConfiguration.getMethod("initialize");
      return (OpenTelemetry) initialize.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(
          "OpenTelemetrySdkAutoConfiguration detected on classpath "
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
}
