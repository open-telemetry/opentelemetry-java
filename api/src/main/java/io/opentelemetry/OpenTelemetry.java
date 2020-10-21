/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.spi.BaggageManagerFactory;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>The default for the OpenTelemetry API will include any {@link
 * io.opentelemetry.trace.spi.TracerProviderFactory}, {@link
 * io.opentelemetry.metrics.spi.MeterProviderFactory}, or {@link
 * io.opentelemetry.baggage.spi.BaggageManagerFactory} found on the classpath, or otherwise will be
 * default, with no-op behavior.
 *
 * @see TracerProvider
 * @see MeterProvider
 * @see BaggageManager
 */
public interface OpenTelemetry {

  /** Returns the globally registered {@link TracerProvider}. */
  static TracerProvider getTracerProvider() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMyTracerProvider();
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
  static Tracer getTracer(String instrumentationName) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMyTracer(instrumentationName);
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
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   */
  static Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry()
        .getMyTracer(instrumentationName, instrumentationVersion);
  }

  /** Returns the globally registered {@link MeterProvider}. */
  static MeterProvider getMeterProvider() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMyMeterProvider();
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
  static Meter getMeter(String instrumentationName) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMyMeter(instrumentationName);
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
  static Meter getMeter(String instrumentationName, String instrumentationVersion) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry()
        .getMyMeter(instrumentationName, instrumentationVersion);
  }

  /** Returns the globally registered {@link BaggageManager}. */
  static BaggageManager getBaggageManager() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMyBaggageManager();
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static ContextPropagators getPropagators() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMyPropagators();
  }

  /**
   * Sets the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static void setPropagators(ContextPropagators propagators) {
    requireNonNull(propagators, "propagators");
    DefaultOpenTelemetry.setGlobalOpenTelemetry(
        ((DefaultOpenTelemetry) DefaultOpenTelemetry.getGlobalOpenTelemetry())
            .toBuilder()
            .setPropagators(propagators)
            .build());
  }

  /** Returns the {@link TracerProvider} for this {@link OpenTelemetry}. */
  TracerProvider getMyTracerProvider();

  /**
   * Gets or creates a named tracer instance from the {@link TracerProvider} for this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  default Tracer getMyTracer(String instrumentationName) {
    return getTracerProvider().get(instrumentationName);
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
  default Tracer getMyTracer(String instrumentationName, String instrumentationVersion) {
    return getTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link MeterProvider} for this {@link OpenTelemetry}. */
  MeterProvider getMyMeterProvider();

  /**
   * Gets or creates a named meter instance from the {@link MeterProvider} in this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  default Meter getMyMeter(String instrumentationName) {
    return getMeterProvider().get(instrumentationName);
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
  default Meter getMyMeter(String instrumentationName, String instrumentationVersion) {
    return getMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link BaggageManager} for this {@link OpenTelemetry}. */
  BaggageManager getMyBaggageManager();

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getMyPropagators();
}
