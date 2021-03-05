/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.concurrent.Callable;

/** Provides easy mechanisms for wrapping standard Java constructs with an OpenTelemetry Span. */
@SuppressWarnings("InconsistentOverloads")
public class ExtendedTracer {

  private final Tracer tracer;

  /** Create a new {@link ExtendedTracer} that wraps the provided Tracer. */
  public ExtendedTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  /** Wrap the provided {@link Runnable} with a {@link Span} with the provided name. */
  public Runnable wrap(String spanName, Runnable runnable) {
    return wrap(tracer, spanName, runnable);
  }

  /**
   * Wrap the provided {@link Runnable} with a {@link Span} with the provided name. Uses the
   * provided {@link Tracer} for the functionality.
   */
  public static Runnable wrap(Tracer tracer, String spanName, Runnable runnable) {
    return () -> {
      Span span = tracer.spanBuilder(spanName).startSpan();
      try (Scope scope = span.makeCurrent()) {
        runnable.run();
      } catch (Throwable e) {
        span.recordException(e);
        throw e;
      } finally {
        span.end();
      }
    };
  }

  /** Wrap the provided {@link Callable} with a {@link Span} with the provided name. */
  public <T> Callable<T> wrap(String spanName, Callable<T> callable) {
    return wrap(tracer, spanName, callable);
  }

  /**
   * Wrap the provided {@link Callable} with a {@link Span} with the provided name. Uses the
   * provided {@link Tracer} for the functionality.
   */
  public static <T> Callable<T> wrap(Tracer tracer, String spanName, Callable<T> callable) {
    return () -> {
      Span span = tracer.spanBuilder(spanName).startSpan();
      try (Scope scope = span.makeCurrent()) {
        return callable.call();
      } catch (Throwable e) {
        span.recordException(e);
        throw e;
      } finally {
        span.end();
      }
    };
  }
}
