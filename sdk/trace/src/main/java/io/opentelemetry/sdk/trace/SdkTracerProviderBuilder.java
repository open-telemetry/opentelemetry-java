/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Builder of {@link SdkTracerProvider}. */
public final class SdkTracerProviderBuilder {
  private static final Sampler DEFAULT_SAMPLER = Sampler.parentBased(Sampler.alwaysOn());

  private final List<SpanProcessor> spanProcessors = new ArrayList<>();

  private Clock clock = SystemClock.getInstance();
  private IdGenerator idsGenerator = IdGenerator.random();
  private Resource resource = Resource.getDefault();
  private Supplier<SpanLimits> traceConfigSupplier = SpanLimits::getDefault;
  private Sampler sampler = DEFAULT_SAMPLER;

  /**
   * Assign a {@link Clock}.
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
   * Assign an {@link IdGenerator}.
   *
   * @param idGenerator A generator for trace and span ids. Note: this should be thread-safe and as
   *     contention free as possible.
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
   * Assign an initial {@link SpanLimits} that should be used with this SDK.
   *
   * @return this
   */
  public SdkTracerProviderBuilder setSpanLimits(SpanLimits spanLimits) {
    requireNonNull(spanLimits, "traceConfig");
    this.traceConfigSupplier = () -> spanLimits;
    return this;
  }

  /**
   * Assign a {@link Supplier} of {@link SpanLimits}. {@link SpanLimits} will be retrieved each time
   * a {@link io.opentelemetry.api.trace.Span} is started.
   *
   * @return this
   */
  public SdkTracerProviderBuilder setSpanLimits(Supplier<SpanLimits> traceConfigSupplier) {
    requireNonNull(traceConfigSupplier, "traceConfig");
    this.traceConfigSupplier = traceConfigSupplier;
    return this;
  }

  /** Assign a {@link Sampler} to use for sampling traces. */
  public SdkTracerProviderBuilder setSampler(Sampler sampler) {
    requireNonNull(sampler, "sampler");
    this.sampler = sampler;
    return this;
  }

  /**
   * Add a SpanProcessor to the span pipeline that will be built.
   *
   * @return this
   */
  public SdkTracerProviderBuilder addSpanProcessor(SpanProcessor spanProcessor) {
    spanProcessors.add(spanProcessor);
    return this;
  }

  /**
   * Create a new TraceSdkProvider instance.
   *
   * @return An initialized TraceSdkProvider.
   */
  public SdkTracerProvider build() {
    return new SdkTracerProvider(
        clock, idsGenerator, resource, traceConfigSupplier, sampler, spanProcessors);
  }

  SdkTracerProviderBuilder() {}
}
