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
import javax.annotation.concurrent.ThreadSafe;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>A global singleton can be retrieved by {@link #getGlobalOpenTelemetry()}. The default for the
 * returned {@link OpenTelemetry}, if none has been set via {@link
 * #setGlobalOpenTelemetry(OpenTelemetry)} will be created with any {@link
 * io.opentelemetry.spi.OpenTelemetryFactory}, {@link
 * io.opentelemetry.trace.spi.TracerProviderFactory}, {@link
 * io.opentelemetry.metrics.spi.MeterProviderFactory}, or {@link
 * io.opentelemetry.baggage.spi.BaggageManagerFactory} found on the classpath, or otherwise will be
 * default, with no-op behavior.
 *
 * <p>If using the OpenTelemetry SDK, you may want to instantiate the {@link OpenTelemetry} to
 * provide configuration, for example of {@code Resource} or {@code Sampler}. See {@code
 * OpenTelemetrySdk} and {@code OpenTelemetrySdk.newBuilder} for information on how to construct the
 * SDK {@link OpenTelemetry}.
 *
 * @see TracerProvider
 * @see MeterProvider
 * @see BaggageManager
 */
@ThreadSafe
public interface OpenTelemetry {

  /**
   * Returns the registered global {@link OpenTelemetry}. If no call to {@link
   * #setGlobalOpenTelemetry(OpenTelemetry)} has been made so far, a default {@link OpenTelemetry}
   * composed of functionality any {@link io.opentelemetry.spi.OpenTelemetryFactory}, {@link
   * io.opentelemetry.trace.spi.TracerProviderFactory}, {@link
   * io.opentelemetry.metrics.spi.MeterProviderFactory}, or {@link
   * io.opentelemetry.baggage.spi.BaggageManagerFactory} found on the classpath, or otherwise * will
   * be default, with no-op behavior.
   *
   * @throws IllegalStateException if a provider has been specified by system property using the
   *     interface FQCN but the specified provider cannot be found.
   */
  static OpenTelemetry getGlobalOpenTelemetry() {
    return DefaultOpenTelemetry.getGlobalOpenTelemetry();
  }

  /**
   * Sets the {@link OpenTelemetry} that should be the global instance. Future calls to {@link
   * #getGlobalOpenTelemetry()} will return the {@link OpenTelemetry}. This should be called once as
   * early as possible in your application initialization logic, often in a {@code static} block in
   * your main class.
   */
  static void setGlobalOpenTelemetry(OpenTelemetry openTelemetry) {
    DefaultOpenTelemetry.setGlobalOpenTelemetry(openTelemetry);
  }

  /**
   * Returns the global {@link TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalOpenTelemetry().getTracerProvider()}.
   */
  static TracerProvider getGlobalTracerProvider() {
    return getGlobalOpenTelemetry().getTracerProvider();
  }

  /**
   * Gets or creates a named tracer instance from the global {@link TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationName)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  static Tracer getGlobalTracer(String instrumentationName) {
    return getGlobalTracerProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the global {@link TracerProvider}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationName,
   * instrumentationVersion)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   */
  static Tracer getGlobalTracer(String instrumentationName, String instrumentationVersion) {
    return getGlobalTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns the global {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalOpenTelemetry().getMeterProvider()}.
   */
  static MeterProvider getGlobalMeterProvider() {
    return getGlobalOpenTelemetry().getMeterProvider();
  }

  /**
   * Gets or creates a named meter instance from the global {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationName)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  static Meter getGlobalMeter(String instrumentationName) {
    return getGlobalMeterProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the global {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationName,
   * instrumentationVersion)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  static Meter getGlobalMeter(String instrumentationName, String instrumentationVersion) {
    return getGlobalMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the global {@link BaggageManager}. */
  static BaggageManager getGlobalBaggageManager() {
    return getGlobalOpenTelemetry().getBaggageManager();
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static ContextPropagators getGlobalPropagators() {
    return getGlobalOpenTelemetry().getPropagators();
  }

  /**
   * Sets the globally registered {@link ContextPropagators} for remote propagation of a context.
   */
  static void setGlobalPropagators(ContextPropagators propagators) {
    requireNonNull(propagators, "propagators");
    setGlobalOpenTelemetry(
        getGlobalOpenTelemetry().toBuilder().setPropagators(propagators).build());
  }

  /** Returns the {@link TracerProvider} for this {@link OpenTelemetry}. */
  TracerProvider getTracerProvider();

  /**
   * Gets or creates a named tracer instance from the {@link TracerProvider} for this {@link
   * OpenTelemetry}.
   *
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationName)}.
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
   * <p>This is a shortcut method for {@code getTracerProvider().get(instrumentationName,
   * instrumentationVersion)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return getTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link MeterProvider} for this {@link OpenTelemetry}. */
  MeterProvider getMeterProvider();

  /**
   * Gets or creates a named meter instance from the {@link MeterProvider} in this {@link
   * OpenTelemetry}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationName)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  default Meter getMeter(String instrumentationName) {
    return getMeterProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the {@link MeterProvider} in this
   * {@link OpenTelemetry}.
   *
   * <p>This is a shortcut method for {@code getMeterProvider().get(instrumentationName,
   * instrumentationVersion)}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  default Meter getMeter(String instrumentationName, String instrumentationVersion) {
    return getMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link BaggageManager} for this {@link OpenTelemetry}. */
  BaggageManager getBaggageManager();

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();

  /** Returns a new {@link Builder} with the configuration of this {@link OpenTelemetry}. */
  Builder<?> toBuilder();

  interface Builder<T extends Builder<T>> {

    /** Sets the {@link TracerProvider} to use. */
    T setTracerProvider(TracerProvider tracerProvider);

    /** Sets the {@link MeterProvider} to use. */
    T setMeterProvider(MeterProvider meterProvider);

    /** Sets the {@link BaggageManager} to use. */
    T setBaggageManager(BaggageManager baggageManager);

    /** Sets the {@link ContextPropagators} to use. */
    T setPropagators(ContextPropagators propagators);

    /** Returns a new {@link OpenTelemetry} based on the configuration in this {@link Builder}. */
    OpenTelemetry build();
  }
}
