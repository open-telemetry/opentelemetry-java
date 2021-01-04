/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import java.util.ArrayList;
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

  // Reads and writes are atomic for reference variables. Use volatile to ensure that these
  // operations are visible on other CPUs as well.
  private volatile Supplier<TraceConfig> traceConfigSupplier;
  private volatile SpanProcessor activeSpanProcessor;

  @GuardedBy("lock")
  @Nullable
  private volatile CompletableResultCode isStopped = null;

  @GuardedBy("lock")
  private final List<SpanProcessor> registeredSpanProcessors;

  TracerSharedState(
      Clock clock,
      IdGenerator idGenerator,
      Resource resource,
      Supplier<TraceConfig> traceConfigSupplier,
      List<SpanProcessor> spanProcessors) {
    this.clock = clock;
    this.idGenerator = idGenerator;
    this.resource = resource;
    this.traceConfigSupplier = traceConfigSupplier;
    this.registeredSpanProcessors = new ArrayList<>(spanProcessors);
    activeSpanProcessor = SpanProcessor.composite(registeredSpanProcessors);
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

  /**
   * Returns the active {@code TraceConfig}.
   *
   * @return the active {@code TraceConfig}.
   */
  TraceConfig getActiveTraceConfig() {
    return traceConfigSupplier.get();
  }

  /**
   * Updates the active {@link TraceConfig}.
   *
   * @param traceConfig the new active {@code TraceConfig}.
   */
  void updateActiveTraceConfig(TraceConfig traceConfig) {
    traceConfigSupplier = () -> traceConfig;
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
   * Adds a new {@code SpanProcessor}.
   *
   * @param spanProcessor the new {@code SpanProcessor} to be added.
   */
  void addSpanProcessor(SpanProcessor spanProcessor) {
    synchronized (lock) {
      registeredSpanProcessors.add(spanProcessor);
      activeSpanProcessor = SpanProcessor.composite(registeredSpanProcessors);
    }
  }

  /**
   * Returns {@code true} if tracing is stopped.
   *
   * @return {@code true} if tracing is stopped.
   */
  boolean isStopped() {
    synchronized (lock) {
      return isStopped != null && isStopped.isSuccess();
    }
  }

  /**
   * Stops tracing, including shutting down processors and set to {@code true} {@link #isStopped()}.
   *
   * @return a {@link CompletableResultCode} that will be completed when the span processor is shut
   *     down.
   */
  CompletableResultCode stop() {
    synchronized (lock) {
      if (isStopped != null) {
        return isStopped;
      }
      isStopped = activeSpanProcessor.shutdown();
      return isStopped;
    }
  }
}
