/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.concurrent.Callable;

/**
 * Provides easy mechanisms for wrapping standard Java constructs with an OpenTelemetry Span.
 *
 * <p>Todo: figure out a better name for this.
 */
@SuppressWarnings("InconsistentOverloads")
public class Spanalyzer {

  private final Tracer tracer;

  /** todo: javadoc me. */
  public Spanalyzer(Tracer tracer) {
    this.tracer = tracer;
  }

  /** todo: javadoc me. */
  public Runnable wrap(String spanName, Runnable runnable) {
    return wrap(tracer, spanName, runnable);
  }

  /** todo: javadoc me. */
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

  /** todo: javadoc me. */
  public <T> Callable<T> wrap(String spanName, Callable<T> callable) {
    return wrap(tracer, spanName, callable);
  }

  /** todo: javadoc me. */
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
