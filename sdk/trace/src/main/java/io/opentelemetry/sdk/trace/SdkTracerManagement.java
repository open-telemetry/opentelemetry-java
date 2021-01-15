/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * "Management" interface for the Tracing SDK. This interface exposes methods for configuring the
 * Tracing SDK, as well as several lifecycle methods.
 */
public interface SdkTracerManagement extends Closeable {

  /**
   * Returns the active {@code TraceConfig}.
   *
   * @return the active {@code TraceConfig}.
   */
  TraceConfig getActiveTraceConfig();

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
   *
   * @return a {@link CompletableResultCode} which is completed when all the span processors have
   *     been shut down.
   */
  CompletableResultCode shutdown();

  /**
   * Requests the active span processor to process all span events that have not yet been processed
   * and returns a {@link CompletableResultCode} which is completed when the flush is finished.
   *
   * @see SpanProcessor#forceFlush()
   */
  CompletableResultCode forceFlush();

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
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
