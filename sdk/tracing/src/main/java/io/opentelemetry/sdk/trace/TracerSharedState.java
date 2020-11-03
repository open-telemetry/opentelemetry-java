/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;

// Represents the shared state/config between all Tracers created by the same TracerProvider.
final class TracerSharedState {
  private final Object lock = new Object();
  private final Clock clock;
  private final IdGenerator idsGenerator;
  private final Resource resource;

  // Reads and writes are atomic for reference variables. Use volatile to ensure that these
  // operations are visible on other CPUs as well.
  private volatile TraceConfig activeTraceConfig = TraceConfig.getDefault();
  private volatile SpanProcessor activeSpanProcessor = NoopSpanProcessor.getInstance();
  private volatile boolean isStopped = false;

  @GuardedBy("lock")
  private final List<SpanProcessor> registeredSpanProcessors = new ArrayList<>();

  TracerSharedState(Clock clock, IdGenerator idsGenerator, Resource resource) {
    this.clock = clock;
    this.idsGenerator = idsGenerator;
    this.resource = resource;
  }

  Clock getClock() {
    return clock;
  }

  IdGenerator getIdsGenerator() {
    return idsGenerator;
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
    return activeTraceConfig;
  }

  /**
   * Updates the active {@link TraceConfig}.
   *
   * @param traceConfig the new active {@code TraceConfig}.
   */
  void updateActiveTraceConfig(TraceConfig traceConfig) {
    activeTraceConfig = traceConfig;
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
      activeSpanProcessor = MultiSpanProcessor.create(registeredSpanProcessors);
    }
  }

  /**
   * Returns {@code true} if tracing is stopped.
   *
   * @return {@code true} if tracing is stopped.
   */
  boolean isStopped() {
    return isStopped;
  }

  /**
   * Stops tracing, including shutting down processors and set to {@code true} {@link #isStopped()}.
   */
  void stop() {
    synchronized (lock) {
      if (isStopped) {
        return;
      }
      activeSpanProcessor.shutdown().join(10, TimeUnit.SECONDS);
      isStopped = true;
    }
  }
}
