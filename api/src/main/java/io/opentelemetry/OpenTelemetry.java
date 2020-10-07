/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.DefaultBaggageManager;
import io.opentelemetry.baggage.spi.BaggageManagerFactory;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MeterProviderFactory;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import java.util.ServiceLoader;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link Meter}
 * and {@link BaggageManager}.
 *
 * <p>The telemetry objects are lazy-loaded singletons resolved via {@link ServiceLoader} mechanism.
 *
 * @see TracerProvider
 * @see MeterProviderFactory
 * @see BaggageManagerFactory
 */
@ThreadSafe
public interface OpenTelemetry {

  static OpenTelemetry getGlobalOpenTelemetry() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry();
  }

  static void setGlobalOpenTelemetry(OpenTelemetry openTelemetry) {
    DefaultOpenTelemetry.setGlobalOpenTelemetry(openTelemetry);
  }

  /**
   * Returns a singleton {@link TracerProvider}.
   *
   * @return registered TracerProvider or default via {@link DefaultTracerProvider#getInstance()}.
   * @throws IllegalStateException if a specified TracerProvider (via system properties) could not
   *     be found.
   * @since 0.1.0
   */
  static TracerProvider getGlobalTracerProvider() {
    return getGlobalOpenTelemetry().getTracerProvider();
  }

  /**
   * Gets or creates a named tracer instance.
   *
   * <p>This is a shortcut method for <code>getTracerProvider().get(instrumentationName)</code>.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   * @since 0.5.0
   */
  static Tracer getGlobalTracer(String instrumentationName) {
    return getGlobalTracerProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance.
   *
   * <p>This is a shortcut method for <code>
   * getTracerProvider().get(instrumentationName, instrumentationVersion)</code>.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   * @since 0.5.0
   */
  static Tracer getGlobalTracer(String instrumentationName, String instrumentationVersion) {
    return getGlobalTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns a singleton {@link MeterProvider}.
   *
   * @return registered MeterProvider or default via {@link DefaultMeterProvider#getInstance()}.
   * @throws IllegalStateException if a specified MeterProvider (via system properties) could not be
   *     found.
   * @since 0.1.0
   */
  static MeterProvider getGlobalMeterProvider() {
    return getGlobalOpenTelemetry().getMeterProvider();
  }

  /**
   * Gets or creates a named meter instance.
   *
   * <p>This is a shortcut method for <code>getMeterProvider().get(instrumentationName)</code>.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   * @since 0.5.0
   */
  static Meter getGlobalMeter(String instrumentationName) {
    return getGlobalMeterProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance.
   *
   * <p>This is a shortcut method for <code>
   * getMeterProvider().get(instrumentationName, instrumentationVersion)</code>.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   * @since 0.5.0
   */
  static Meter getGlobalMeter(String instrumentationName, String instrumentationVersion) {
    return getGlobalMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns a singleton {@link BaggageManager}.
   *
   * @return registered manager or default via {@link DefaultBaggageManager#getInstance()}.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @since 0.1.0
   */
  static BaggageManager getGlobalBaggageManager() {
    return getGlobalOpenTelemetry().getBaggageManager();
  }

  /**
   * Returns a {@link ContextPropagators} object, which can be used to access the set of registered
   * propagators for each supported format.
   *
   * @return registered propagators container, defaulting to a {@link ContextPropagators} object
   *     with {@link HttpTraceContext} registered.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @since 0.3.0
   */
  static ContextPropagators getGlobalPropagators() {
    return getGlobalOpenTelemetry().getPropagators();
  }

  TracerProvider getTracerProvider();

  default Tracer getTracer(String instrumentationName) {
    return getTracerProvider().get(instrumentationName);
  }

  default Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return getTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  MeterProvider getMeterProvider();

  default Meter getMeter(String instrumentationName) {
    return getMeterProvider().get(instrumentationName);
  }

  default Meter getMeter(String instrumentationName, String instrumentationVersion) {
    return getMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  BaggageManager getBaggageManager();

  ContextPropagators getPropagators();

  /**
   * Sets the {@link ContextPropagators} object, which can be used to access the set of registered
   * propagators for each supported format.
   *
   * @param propagators the {@link ContextPropagators} object to be registered.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @throws NullPointerException if {@code propagators} is {@code null}.
   * @since 0.3.0
   */
  static void setPropagators(ContextPropagators propagators) {
    requireNonNull(propagators, "propagators");
    setGlobalOpenTelemetry(getGlobalOpenTelemetry().withPropagators(propagators));
  }

  OpenTelemetry withPropagators(ContextPropagators propagators);
}
