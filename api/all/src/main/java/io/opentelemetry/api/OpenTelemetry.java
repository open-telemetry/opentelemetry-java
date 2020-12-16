/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.spi.OpenTelemetryFactory;
import io.opentelemetry.spi.metrics.MeterProviderFactory;
import io.opentelemetry.spi.trace.TracerProviderFactory;
import java.util.Objects;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>A global singleton can be retrieved by {@link #get()}. The default for the returned {@link
 * OpenTelemetry}, if none has been set via {@link #set(OpenTelemetry)}, will be created with any
 * {@link OpenTelemetryFactory}, {@link TracerProviderFactory} or {@link MeterProviderFactory} found
 * on the classpath, or otherwise will be default, with no-op behavior.
 *
 * <p>If using the OpenTelemetry SDK, you may want to instantiate the {@link OpenTelemetry} to
 * provide configuration, for example of {@code Resource} or {@code Sampler}. See {@code
 * OpenTelemetrySdk} and {@code OpenTelemetrySdk.builder} for information on how to construct the
 * SDK {@link OpenTelemetry}.
 *
 * @see TracerProvider
 * @see MeterProvider
 * @see ContextPropagators
 */
public interface OpenTelemetry {

  /**
   * Returns the registered global {@link OpenTelemetry}.
   *
   * @deprecated use {@link GlobalOpenTelemetry#get()}
   */
  @Deprecated
  static OpenTelemetry get() {
    return GlobalOpenTelemetry.get();
  }

  /**
   * Sets the {@link OpenTelemetry} that should be the global instance.
   *
   * @deprecated use {@link GlobalOpenTelemetry#set(OpenTelemetry)}
   */
  @Deprecated
  static void set(OpenTelemetry openTelemetry) {
    GlobalOpenTelemetry.set(openTelemetry);
  }

  /**
   * Returns the globally registered {@link TracerProvider}.
   *
   * @deprecated use {@link GlobalOpenTelemetry#getTracerProvider()}
   */
  @Deprecated
  static TracerProvider getGlobalTracerProvider() {
    return get().getTracerProvider();
  }

  /**
   * Gets or creates a named tracer instance from the globally registered {@link TracerProvider}.
   *
   * @deprecated use {@link GlobalOpenTelemetry#getTracer(String)}
   */
  @Deprecated
  static Tracer getGlobalTracer(String instrumentationName) {
    return get().getTracer(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the globally registered {@link
   * TracerProvider}.
   *
   * @deprecated use {@link GlobalOpenTelemetry#getTracer(String, String)}
   */
  @Deprecated
  static Tracer getGlobalTracer(String instrumentationName, String instrumentationVersion) {
    return get().getTracer(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns the globally registered {@link MeterProvider}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  static MeterProvider getGlobalMeterProvider() {
    return get().getMeterProvider();
  }

  /**
   * Gets or creates a named meter instance from the globally registered {@link MeterProvider}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  static Meter getGlobalMeter(String instrumentationName) {
    return get().getMeter(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the globally registered {@link
   * MeterProvider}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  static Meter getGlobalMeter(String instrumentationName, String instrumentationVersion) {
    return get().getMeter(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns the globally registered {@link ContextPropagators} for remote propagation of a context.
   *
   * @deprecated use {@link GlobalOpenTelemetry#getPropagators()}
   */
  @Deprecated
  static ContextPropagators getGlobalPropagators() {
    return get().getPropagators();
  }

  /**
   * Sets the globally registered {@link ContextPropagators} for remote propagation of a context.
   *
   * @deprecated this will be removed soon, create a new instance if necessary.
   */
  @Deprecated
  static void setGlobalPropagators(ContextPropagators propagators) {
    Objects.requireNonNull(propagators, "propagators");
    get().setPropagators(propagators);
  }

  /**
   * Sets the propagators that this instance should contain.
   *
   * @deprecated this will be removed soon, create a new instance if necessary.
   */
  @Deprecated
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

  /**
   * Returns the {@link MeterProvider} for this {@link OpenTelemetry}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  MeterProvider getMeterProvider();

  /**
   * Gets or creates a named meter instance from the {@link MeterProvider} in this {@link
   * OpenTelemetry}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  default Meter getMeter(String instrumentationName) {
    return getMeterProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the {@link MeterProvider} in this
   * {@link OpenTelemetry}.
   *
   * @deprecated this will be removed soon in preparation for the initial otel release.
   */
  @Deprecated
  default Meter getMeter(String instrumentationName, String instrumentationVersion) {
    return getMeterProvider().get(instrumentationName, instrumentationVersion);
  }

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();

  /**
   * Returns a new {@link OpenTelemetryBuilder}.
   *
   * @deprecated use {@link DefaultOpenTelemetry#builder()}.
   */
  @Deprecated
  static OpenTelemetryBuilder<?> builder() {
    return new DefaultOpenTelemetryBuilder();
  }
}
