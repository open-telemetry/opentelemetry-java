/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** SDK implementation for {@link TracerProvider}. */
public final class SdkTracerProvider implements TracerProvider, Closeable {
  private static final Logger logger = Logger.getLogger(SdkTracerProvider.class.getName());
  static final String DEFAULT_TRACER_NAME = "";
  private final TracerSharedState sharedState;
  private final ComponentRegistry<SdkTracer> tracerSdkComponentRegistry;
  // deliberately not volatile because of performance concerns
  // - which means its eventually consistent
  private ScopeConfigurator<TracerConfig> tracerConfigurator;

  /**
   * Returns a new {@link SdkTracerProviderBuilder} for {@link SdkTracerProvider}.
   *
   * @return a new {@link SdkTracerProviderBuilder} for {@link SdkTracerProvider}.
   */
  public static SdkTracerProviderBuilder builder() {
    return new SdkTracerProviderBuilder();
  }

  @SuppressWarnings("NonApiType")
  SdkTracerProvider(
      Clock clock,
      IdGenerator idsGenerator,
      Resource resource,
      Supplier<SpanLimits> spanLimitsSupplier,
      Sampler sampler,
      List<SpanProcessor> spanProcessors,
      ScopeConfigurator<TracerConfig> tracerConfigurator,
      ExceptionAttributeResolver exceptionAttributeResolver,
      Supplier<MeterProvider> meterProvider) {
    this.sharedState =
        new TracerSharedState(
            clock,
            idsGenerator,
            resource,
            spanLimitsSupplier,
            sampler,
            spanProcessors,
            exceptionAttributeResolver,
            new SdkTracerMetrics(meterProvider));
    this.tracerSdkComponentRegistry =
        new ComponentRegistry<>(
            instrumentationScopeInfo ->
                SdkTracer.create(
                    sharedState,
                    instrumentationScopeInfo,
                    getTracerConfig(instrumentationScopeInfo)));
    this.tracerConfigurator = tracerConfigurator;
  }

  private TracerConfig getTracerConfig(InstrumentationScopeInfo instrumentationScopeInfo) {
    TracerConfig tracerConfig = tracerConfigurator.apply(instrumentationScopeInfo);
    return tracerConfig == null ? TracerConfig.defaultConfig() : tracerConfig;
  }

  @Override
  public Tracer get(String instrumentationScopeName) {
    return tracerBuilder(instrumentationScopeName).build();
  }

  @Override
  public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
    return tracerBuilder(instrumentationScopeName)
        .setInstrumentationVersion(instrumentationScopeVersion)
        .build();
  }

  @Override
  public TracerBuilder tracerBuilder(@Nullable String instrumentationScopeName) {
    // Per the spec, both null and empty are "invalid" and a default value should be used.
    if (instrumentationScopeName == null || instrumentationScopeName.isEmpty()) {
      logger.fine("Tracer requested without instrumentation scope name.");
      instrumentationScopeName = DEFAULT_TRACER_NAME;
    }
    return new SdkTracerBuilder(tracerSdkComponentRegistry, instrumentationScopeName);
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
   * Updates the tracer configurator, which computes {@link TracerConfig} for each {@link
   * InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkTracerProviderUtil#setTracerConfigurator(SdkTracerProvider, ScopeConfigurator)}.
   *
   * @see TracerConfig#configuratorBuilder()
   */
  void setTracerConfigurator(ScopeConfigurator<TracerConfig> tracerConfigurator) {
    this.tracerConfigurator = tracerConfigurator;
    this.tracerSdkComponentRegistry
        .getComponents()
        .forEach(
            sdkTracer ->
                sdkTracer.updateTracerConfig(
                    getTracerConfig(sdkTracer.getInstrumentationScopeInfo())));
  }

  /**
   * Attempts to stop all the activity for {@link Tracer}s created by this provider. Calls {@link
   * SpanProcessor#shutdown()} for all registered {@link SpanProcessor}s.
   *
   * <p>The returned {@link CompletableResultCode} will be completed when all the Spans are
   * processed.
   *
   * <p>After this is called, newly created {@code Span}s will be no-ops.
   *
   * <p>After this is called, further attempts at re-using this instance will result in undefined
   * behavior. It should be considered a terminal operation for the SDK.
   *
   * @return a {@link CompletableResultCode} which is completed when all the span processors have
   *     been shut down.
   */
  public CompletableResultCode shutdown() {
    if (sharedState.hasBeenShutdown()) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
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
   * Attempts to stop all the activity for {@link Tracer}s created by this provider. Calls {@link
   * SpanProcessor#shutdown()} for all registered {@link SpanProcessor}s.
   *
   * <p>This operation may block until all the Spans are processed. Must be called before turning
   * off the main application to ensure all data are processed and exported.
   *
   * <p>After this is called, newly created {@code Span}s will be no-ops.
   *
   * <p>After this is called, further attempts at re-using this instance will result in undefined
   * behavior. It should be considered a terminal operation for the SDK.
   */
  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "SdkTracerProvider{"
        + "clock="
        + sharedState.getClock()
        + ", idGenerator="
        + sharedState.getIdGenerator()
        + ", resource="
        + sharedState.getResource()
        + ", spanLimitsSupplier="
        + sharedState.getSpanLimits()
        + ", sampler="
        + sharedState.getSampler()
        + ", spanProcessor="
        + sharedState.getActiveSpanProcessor()
        + ", tracerConfigurator="
        + tracerConfigurator
        + '}';
  }
}
