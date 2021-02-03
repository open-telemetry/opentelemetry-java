/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

// Represents the shared state/config between all Tracers created by the same TracerProvider.
final class TracerSharedState {
  private final Object lock = new Object();
  private final Clock clock;
  private final IdGenerator idGenerator;
  private final Resource resource;

  private final Supplier<SpanLimits> traceConfigSupplier;
  private final Sampler sampler;
  private final SpanProcessor activeSpanProcessor;

  @GuardedBy("lock")
  @Nullable
  private volatile CompletableResultCode shutdownResult = null;

  TracerSharedState(
      Clock clock,
      IdGenerator idGenerator,
      Resource resource,
      Supplier<SpanLimits> traceConfigSupplier,
      Sampler sampler,
      List<SpanProcessor> spanProcessors) {
    this.clock = clock;
    this.idGenerator = idGenerator;
    this.resource = resource;
    this.traceConfigSupplier = traceConfigSupplier;
    this.sampler = sampler;
    activeSpanProcessor = SpanProcessor.composite(spanProcessors);
  }

  Clock getClock() {
    return clock;
  }

  IdGenerator getIdGenerator() {
    return idGenerator;
  }

  Resource getResource() {
    return resource;
  }

  /** Returns the current {@link SpanLimits}. */
  SpanLimits getSpanLimits() {
    return traceConfigSupplier.get();
  }

  /** Returns the configured {@link Sampler}. */
  Sampler getSampler() {
    return sampler;
  }

  /**
   * Returns the active {@code SpanProcessor}.
   *
   * @return the active {@code SpanProcessor}.
   */
  SpanProcessor getActiveSpanProcessor() {
    return activeSpanProcessor;
  }

  /**
   * Returns {@code true} if tracing is stopped.
   *
   * @return {@code true} if tracing is stopped.
   */
  boolean isStopped() {
    synchronized (lock) {
      return shutdownResult != null && shutdownResult.isSuccess();
    }
  }

  /**
   * Stops tracing, including shutting down processors and set to {@code true} {@link #isStopped()}.
   *
   * @return a {@link CompletableResultCode} that will be completed when the span processor is shut
   *     down.
   */
  CompletableResultCode shutdown() {
    synchronized (lock) {
      if (shutdownResult != null) {
        return shutdownResult;
      }
      shutdownResult = activeSpanProcessor.shutdown();
      return shutdownResult;
    }
  }
}
