/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>The default for the OpenTelemetry API will include any {@link
 * io.opentelemetry.trace.spi.TracerProviderFactory} or {@link
 * io.opentelemetry.metrics.spi.MeterProviderFactory} found on the classpath, or otherwise will be
 * default, with no-op behavior.
 *
 * @see TracerProvider
 * @see MeterProvider
 */
public interface OpenTelemetry {

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
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   */
  static Tracer getGlobalTracer(String instrumentationName, String instrumentationVersion) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry()
        .getTracer(instrumentationName, instrumentationVersion);
  }

  /** Returns the globally registered {@link MeterProvider}. */
  static MeterProvider getGlobalMeterProvider() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMeterProvider();
  }

  /**
   * Gets or creates a named meter instance from the globally registered {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalMeterProvider().get(instrumentationName)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  static Meter getGlobalMeter(String instrumentationName) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry().getMeter(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the globally registered {@link
   * MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalMeterProvider().get(instrumentationName,
   * instrumentationVersion)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  static Meter getGlobalMeter(String instrumentationName, String instrumentationVersion) {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry()
        .getMeter(instrumentationName, instrumentationVersion);
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
    DefaultOpenTelemetry.setGlobalOpenTelemetry(
        ((DefaultOpenTelemetry) DefaultOpenTelemetry.getGlobalOpenTelemetry())
            .toBuilder().setPropagators(propagators).build());
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

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();
}
