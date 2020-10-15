/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>A global singleton can be retrieved by {@link #get()}. The default for the returned {@link
 * OpenTelemetry}, will include any {@link io.opentelemetry.trace.spi.TracerProviderFactory}, {@link
 * io.opentelemetry.metrics.spi.MeterProviderFactory}, or {@link
 * io.opentelemetry.baggage.spi.BaggageManagerFactory} found on the classpath, or otherwise will be
 * default, with no-op behavior.
 *
 * @see TracerProvider
 * @see MeterProvider
 * @see BaggageManager
 */
public interface OpenTelemetry {

  /**
   * Returns the registered global {@link OpenTelemetry}, a default {@link OpenTelemetry} composed
   * of functionality any {@link io.opentelemetry.trace.spi.TracerProviderFactory}, {@link
   * io.opentelemetry.metrics.spi.MeterProviderFactory}, or {@link
   * io.opentelemetry.baggage.spi.BaggageManagerFactory} found on the classpath, or otherwise will
   * be default, with no-op behavior.
   *
   * @throws IllegalStateException if a provider has been specified by system property using the
   *     interface FQCN but the specified provider cannot be found.
   */
  static OpenTelemetry get() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry();
  }

  /** Returns the globally registered {@link TracerProvider}. */
  static TracerProvider getGlobalTracerProvider() {
    return get().getTracerProvider();
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
    return get().getTracer(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the globally registered {@link
   * TracerProvider}.
   *
   * <p>This is a shortcut method for @codegetGlobalTracerProvider().get(instrumentationName,
   * instrumentationVersion)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   */
  static Tracer getGlobalTracer(String instrumentationName, String instrumentationVersion) {
    return get().getTracer(instrumentationName, instrumentationVersion);
  }

  /** Returns the globally registered {@link MeterProvider}. */
  static MeterProvider getGlobalMeterProvider() {
    return get().getMeterProvider();
  }

  /**
   * Gets or creates a named meter instance from the globally registered {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationName)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  static Meter getGlobalMeter(String instrumentationName) {
    return get().getMeter(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the globally registered {@link
   * MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationName,
   * instrumentationVersion)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  static Meter getGlobalMeter(String instrumentationName, String instrumentationVersion) {
    return get().getMeter(instrumentationName, instrumentationVersion);
  }

  /** Returns the globally registered {@link BaggageManager}. */
  static BaggageManager getGlobalBaggageManager() {
    return get().getBaggageManager();
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static ContextPropagators getGlobalPropagators() {
    return get().getPropagators();
  }

  /**
   * Sets the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static void setGlobalPropagators(ContextPropagators propagators) {
    requireNonNull(propagators, "propagators");
    DefaultOpenTelemetry.setGlobalOpenTelemetry(
        ((DefaultOpenTelemetry) get()).toBuilder().setPropagators(propagators).build());
  }

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
    return getGlobalTracerProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the {@link TracerProvider} in this
   * {@link OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return getGlobalTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link MeterProvider} for this {@link OpenTelemetry}. */
  MeterProvider getMeterProvider();

  /**
   * Gets or creates a named meter instance from the {@link MeterProvider} in this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  default Meter getMeter(String instrumentationName) {
    return getGlobalMeterProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the {@link MeterProvider} in this
   * {@link OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  default Meter getMeter(String instrumentationName, String instrumentationVersion) {
    return getGlobalMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link BaggageManager} for this {@link OpenTelemetry}. */
  BaggageManager getBaggageManager();

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();
}
