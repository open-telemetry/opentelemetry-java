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

// Represents the shared state/config between all Tracers created by the same TracerProvider.
final class TracerSharedState {
  private final Object lock = new Object();
  private final Clock clock;
  private final IdGenerator idGenerator;
  // tracks whether it is safe to skip id validation on ids from the above generator
  private final boolean idGeneratorSafeToSkipIdValidation;
  private final Resource resource;

  private final Supplier<SpanLimits> spanLimitsSupplier;
  private final Sampler sampler;
  private final SpanProcessor activeSpanProcessor;

  @Nullable private volatile CompletableResultCode shutdownResult = null;

  TracerSharedState(
      Clock clock,
      IdGenerator idGenerator,
      Resource resource,
      Supplier<SpanLimits> spanLimitsSupplier,
      Sampler sampler,
      List<SpanProcessor> spanProcessors) {
    this.clock = clock;
    this.idGenerator = idGenerator;
    this.idGeneratorSafeToSkipIdValidation = idGenerator instanceof RandomIdGenerator;
    this.resource = resource;
    this.spanLimitsSupplier = spanLimitsSupplier;
    this.sampler = sampler;
    activeSpanProcessor = SpanProcessor.composite(spanProcessors);
  }

  Clock getClock() {
    return clock;
  }

  IdGenerator getIdGenerator() {
    return idGenerator;
  }

  boolean isIdGeneratorSafeToSkipIdValidation() {
    return idGeneratorSafeToSkipIdValidation;
  }

  Resource getResource() {
    return resource;
  }

  /** Returns the current {@link SpanLimits}. */
  SpanLimits getSpanLimits() {
    return spanLimitsSupplier.get();
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
   * Returns {@code true} if tracing has been shut down.
   *
   * @return {@code true} if tracing has been shut down.
   */
  boolean hasBeenShutdown() {
    return shutdownResult != null;
  }

  /**
   * Stops tracing, including shutting down processors and set to {@code true} {@link
   * #hasBeenShutdown()}.
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
