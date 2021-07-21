/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * {@code Tracer} provider implementation for {@link TracerProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}. However, if you need a custom implementation of the factory, you can create one
 * as needed.
 */
public final class SdkTracerProvider implements TracerProvider, Closeable {
  private static final Logger logger = Logger.getLogger(SdkTracerProvider.class.getName());
  static final String DEFAULT_TRACER_NAME = "";
  private final TracerSharedState sharedState;
  private final ComponentRegistry<SdkTracer> tracerSdkComponentRegistry;

  /**
   * Returns a new {@link SdkTracerProviderBuilder} for {@link SdkTracerProvider}.
   *
   * @return a new {@link SdkTracerProviderBuilder} for {@link SdkTracerProvider}.
   */
  public static SdkTracerProviderBuilder builder() {
    return new SdkTracerProviderBuilder();
  }

  SdkTracerProvider(
      Clock clock,
      IdGenerator idsGenerator,
      Resource resource,
      Supplier<SpanLimits> spanLimitsSupplier,
      Sampler sampler,
      List<SpanProcessor> spanProcessors) {
    this.sharedState =
        new TracerSharedState(
            clock, idsGenerator, resource, spanLimitsSupplier, sampler, spanProcessors);
    this.tracerSdkComponentRegistry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new SdkTracer(sharedState, instrumentationLibraryInfo));
  }

  @Override
  public Tracer get(String instrumentationName) {
    return tracerBuilder(instrumentationName).build();
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    return tracerBuilder(instrumentationName)
        .setInstrumentationVersion(instrumentationVersion)
        .build();
  }

  @Override
  public TracerBuilder tracerBuilder(@Nullable String instrumentationName) {
    // Per the spec, both null and empty are "invalid" and a default value should be used.
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      logger.fine("Tracer requested without instrumentation name.");
      instrumentationName = DEFAULT_TRACER_NAME;
    }
    return new SdkTracerBuilder(tracerSdkComponentRegistry, instrumentationName);
  }

  /** Returns the {@link SpanLimits} that are currently applied to created spans. */
  public SpanLimits getSpanLimits() {
    return sharedState.getSpanLimits();
  }

  /** Returns the configured {@link Sampler}. */
  public Sampler getSampler() {
    return sharedState.getSampler();
  }

  /**
   * Attempts to stop all the activity for this {@link Tracer}. Calls {@link
   * SpanProcessor#shutdown()} for all registered {@link SpanProcessor}s.
   *
   * <p>The returned {@link CompletableResultCode} will be completed when all the Spans are
   * processed.
   *
   * <p>After this is called, newly created {@code Span}s will be no-ops.
   *
   * <p>After this is called, further attempts at re-using or reconfiguring this instance will
   * result in undefined behavior. It should be considered a terminal operation for the SDK
   * implementation.
   *
   * @return a {@link CompletableResultCode} which is completed when all the span processors have
   *     been shut down.
   */
  public CompletableResultCode shutdown() {
    if (sharedState.hasBeenShutdown()) {
      logger.log(Level.WARNING, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return sharedState.shutdown();
  }

  /**
   * Requests the active span processor to process all span events that have not yet been processed
   * and returns a {@link CompletableResultCode} which is completed when the flush is finished.
   *
   * @see SpanProcessor#forceFlush()
   */
  public CompletableResultCode forceFlush() {
    return sharedState.getActiveSpanProcessor().forceFlush();
  }

  /**
   * Attempts to stop all the activity for this {@link Tracer}. Calls {@link
   * SpanProcessor#shutdown()} for all registered {@link SpanProcessor}s.
   *
   * <p>This operation may block until all the Spans are processed. Must be called before turning
   * off the main application to ensure all data are processed and exported.
   *
   * <p>After this is called, newly created {@code Span}s will be no-ops.
   *
   * <p>After this is called, further attempts at re-using or reconfiguring this instance will
   * result in undefined behavior. It should be considered a terminal operation for the SDK
   * implementation.
   */
  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
