/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.spi.OpenTelemetryFactory;
import io.opentelemetry.spi.trace.TracerProviderFactory;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>A global singleton can be retrieved by {@link #get()}. The default for the returned {@link
 * OpenTelemetry}, if none has been set via {@link #set(OpenTelemetry)}, will be created with any
 * {@link OpenTelemetryFactory}, or {@link TracerProviderFactory} found on the classpath, or
 * otherwise will be default, with no-op behavior.
 *
 * <p>If using the OpenTelemetry SDK, you may want to instantiate the {@link OpenTelemetry} to
 * provide configuration, for example of {@code Resource} or {@code Sampler}. See {@code
 * OpenTelemetrySdk} and {@code OpenTelemetrySdk.builder} for information on how to construct the
 * SDK {@link OpenTelemetry}.
 *
 * @see TracerProvider
 * @see ContextPropagators
 */
public interface OpenTelemetry {

  /**
   * Returns the registered global {@link OpenTelemetry}. If no call to {@link #set(OpenTelemetry)}
   * has been made so far, a default {@link OpenTelemetry} composed of functionality any {@link
   * OpenTelemetryFactory}, or {@link TracerProviderFactory}, found on the classpath, or otherwise
   * will be default, with no-op behavior.
   *
   * @throws IllegalStateException if a provider has been specified by system property using the
   *     interface FQCN but the specified provider cannot be found.
   */
  static OpenTelemetry get() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry();
  }

  /**
   * Sets the {@link OpenTelemetry} that should be the global instance. Future calls to {@link
   * #get()} will return the provided {@link OpenTelemetry} instance. This should be called once as
   * early as possible in your application initialization logic, often in a {@code static} block in
   * your main class.
   */
  static void set(OpenTelemetry openTelemetry) {
    DefaultOpenTelemetry.setGlobalOpenTelemetry(openTelemetry);
  }

  /** Returns the globally registered {@link TracerProvider}. */
  static TracerProvider getGlobalTracerProvider() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getTracerProvider();
  }

  /**
   * Gets or creates a named tracer instance from the globally registered {@link TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalTracerProvider().get(instrumentationName)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  static Tracer getGlobalTracer(String instrumentationName) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getTracer(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the globally registered {@link
   * TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalTracerProvider().get(instrumentationName,
   * instrumentationVersion)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g., "1.0.0").
   * @return a tracer instance.
   */
  static Tracer getGlobalTracer(String instrumentationName, String instrumentationVersion) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry()
        .getTracer(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static ContextPropagators getGlobalPropagators() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getPropagators();
  }

  /**
   * Sets the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static void setGlobalPropagators(ContextPropagators propagators) {
    requireNonNull(propagators, "propagators");
    get().setPropagators(propagators);
  }

  /** Sets the propagators that this instance should contain. */
  void setPropagators(ContextPropagators propagators);

  /** Returns the {@link TracerProvider} for this {@link OpenTelemetry}. */
  TracerProvider getTracerProvider();

  /**
   * Gets or creates a named tracer instance from the {@link TracerProvider} for this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationName) {
    return getTracerProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the {@link TracerProvider} in this
   * {@link OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g., "1.0.0").
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return getTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();

  /** Returns a new {@link OpenTelemetryBuilder}. */
  static OpenTelemetryBuilder<?> builder() {
    return new DefaultOpenTelemetry.Builder();
  }
}
