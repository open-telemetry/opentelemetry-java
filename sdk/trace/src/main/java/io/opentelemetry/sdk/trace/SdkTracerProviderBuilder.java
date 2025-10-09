/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Builder of {@link SdkTracerProvider}. */
public final class SdkTracerProviderBuilder {
  private static final Sampler DEFAULT_SAMPLER = Sampler.parentBased(Sampler.alwaysOn());

  private final List<SpanProcessor> spanProcessors = new ArrayList<>();

  private Clock clock = Clock.getDefault();
  private IdGenerator idsGenerator = IdGenerator.random();
  private Resource resource = Resource.getDefault();
  private Supplier<SpanLimits> spanLimitsSupplier = SpanLimits::getDefault;
  private Sampler sampler = DEFAULT_SAMPLER;
  private ScopeConfiguratorBuilder<TracerConfig> tracerConfiguratorBuilder =
      TracerConfig.configuratorBuilder();
  private ExceptionAttributeResolver exceptionAttributeResolver =
      ExceptionAttributeResolver.getDefault();

  /**
   * Assign a {@link Clock}. {@link Clock} will be used each time a {@link Span} is started, ended
   * or any event is recorded.
   *
   * <p>The {@code clock} must be thread-safe and return immediately (no remote calls, as contention
   * free as possible).
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkTracerProviderBuilder setClock(Clock clock) {
    requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Assign an {@link IdGenerator}. {@link IdGenerator} will be used each time a {@link Span} is
   * started.
   *
   * <p>The {@code idGenerator} must be thread-safe and return immediately (no remote calls, as
   * contention free as possible).
   *
   * @param idGenerator A generator for trace and span ids.
   * @return this
   */
  public SdkTracerProviderBuilder setIdGenerator(IdGenerator idGenerator) {
    requireNonNull(idGenerator, "idGenerator");
    this.idsGenerator = idGenerator;
    return this;
  }

  /**
   * Assign a {@link Resource} to be attached to all Spans created by Tracers.
   *
   * @param resource A Resource implementation.
   * @return this
   */
  public SdkTracerProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Merge a {@link Resource} with the current.
   *
   * @param resource {@link Resource} to merge with current.
   * @since 1.29.0
   */
  public SdkTracerProviderBuilder addResource(Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = this.resource.merge(resource);
    return this;
  }

  /**
   * Assign an initial {@link SpanLimits} that should be used with this SDK.
   *
   * <p>This method is equivalent to calling {@link #setSpanLimits(Supplier)} like this {@code
   * #setSpanLimits(() -> spanLimits)}.
   *
   * @param spanLimits the limits that will be used for every {@link Span}.
   * @return this
   */
  public SdkTracerProviderBuilder setSpanLimits(SpanLimits spanLimits) {
    requireNonNull(spanLimits, "spanLimits");
    this.spanLimitsSupplier = () -> spanLimits;
    return this;
  }

  /**
   * Assign a {@link Supplier} of {@link SpanLimits}. {@link SpanLimits} will be retrieved each time
   * a {@link Span} is started.
   *
   * <p>The {@code spanLimitsSupplier} must be thread-safe and return immediately (no remote calls,
   * as contention free as possible).
   *
   * @param spanLimitsSupplier the supplier that will be used to retrieve the {@link SpanLimits} for
   *     every {@link Span}.
   * @return this
   */
  public SdkTracerProviderBuilder setSpanLimits(Supplier<SpanLimits> spanLimitsSupplier) {
    requireNonNull(spanLimitsSupplier, "spanLimitsSupplier");
    this.spanLimitsSupplier = spanLimitsSupplier;
    return this;
  }

  /**
   * Assign a {@link Sampler} to use for sampling traces. {@link Sampler} will be called each time a
   * {@link Span} is started.
   *
   * <p>The {@code sampler} must be thread-safe and return immediately (no remote calls, as
   * contention free as possible).
   *
   * @param sampler the {@link Sampler} to use for sampling traces.
   * @return this
   */
  public SdkTracerProviderBuilder setSampler(Sampler sampler) {
    requireNonNull(sampler, "sampler");
    this.sampler = sampler;
    return this;
  }

  /**
   * Add a SpanProcessor to the span pipeline that will be built. {@link SpanProcessor} will be
   * called each time a {@link Span} is started or ended.
   *
   * <p>The {@code spanProcessor} must be thread-safe and return immediately (no remote calls, as
   * contention free as possible).
   *
   * @param spanProcessor the processor to be added to the processing pipeline.
   * @return this
   */
  public SdkTracerProviderBuilder addSpanProcessor(SpanProcessor spanProcessor) {
    requireNonNull(spanProcessor, "spanProcessor");
    spanProcessors.add(spanProcessor);
    return this;
  }

  /**
   * Add a SpanProcessor to the beginning of the span pipeline that will be built. {@link
   * SpanProcessor} will be called each time a {@link Span} is started or ended.
   *
   * <p>The {@code spanProcessor} must be thread-safe and return immediately (no remote calls, as
   * contention free as possible).
   *
   * @param spanProcessor the processor to be added to the beginning of the span pipeline.
   * @return this
   * @since 1.50.0
   */
  public SdkTracerProviderBuilder addSpanProcessorFirst(SpanProcessor spanProcessor) {
    requireNonNull(spanProcessor, "spanProcessor");
    spanProcessors.add(0, spanProcessor);
    return this;
  }

  /**
   * Set the tracer configurator, which computes {@link TracerConfig} for each {@link
   * InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkTracerProviderUtil#setTracerConfigurator(SdkTracerProviderBuilder, ScopeConfigurator)}.
   *
   * <p>Overrides any matchers added via {@link #addTracerConfiguratorCondition(Predicate,
   * TracerConfig)}.
   *
   * @see TracerConfig#configuratorBuilder()
   */
  SdkTracerProviderBuilder setTracerConfigurator(
      ScopeConfigurator<TracerConfig> tracerConfigurator) {
    this.tracerConfiguratorBuilder = tracerConfigurator.toBuilder();
    return this;
  }

  /**
   * Adds a condition to the tracer configurator, which computes {@link TracerConfig} for each
   * {@link InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkTracerProviderUtil#addTracerConfiguratorCondition(SdkTracerProviderBuilder, Predicate,
   * TracerConfig)}.
   *
   * <p>Applies after any previously added conditions.
   *
   * <p>If {@link #setTracerConfigurator(ScopeConfigurator)} was previously called, this condition
   * will only be applied if the {@link ScopeConfigurator#apply(Object)} returns null for the
   * matched {@link InstrumentationScopeInfo}(s).
   *
   * @see ScopeConfiguratorBuilder#nameEquals(String)
   * @see ScopeConfiguratorBuilder#nameMatchesGlob(String)
   */
  SdkTracerProviderBuilder addTracerConfiguratorCondition(
      Predicate<InstrumentationScopeInfo> scopeMatcher, TracerConfig tracerConfig) {
    this.tracerConfiguratorBuilder.addCondition(scopeMatcher, tracerConfig);
    return this;
  }

  /**
   * Sets the exception attribute resolver, which resolves {@code exception.*} attributes when
   * {@link Span#recordException(Throwable)} is called.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkTracerProviderUtil#setExceptionAttributeResolver(SdkTracerProviderBuilder,
   * ExceptionAttributeResolver)}.
   */
  SdkTracerProviderBuilder setExceptionAttributeResolver(
      ExceptionAttributeResolver exceptionAttributeResolver) {
    requireNonNull(exceptionAttributeResolver, "exceptionAttributeResolver");
    this.exceptionAttributeResolver = exceptionAttributeResolver;
    return this;
  }

  /**
   * Create a new {@link SdkTracerProvider} instance with the configuration.
   *
   * @return The instance.
   */
  public SdkTracerProvider build() {
    return new SdkTracerProvider(
        clock,
        idsGenerator,
        resource,
        spanLimitsSupplier,
        sampler,
        spanProcessors,
        tracerConfiguratorBuilder.build(),
        exceptionAttributeResolver);
  }

  SdkTracerProviderBuilder() {}
}
