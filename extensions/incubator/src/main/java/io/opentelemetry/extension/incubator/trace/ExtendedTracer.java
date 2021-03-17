/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.concurrent.Callable;

/** Provides easy mechanisms for wrapping standard Java constructs with an OpenTelemetry Span. */
public final class ExtendedTracer implements Tracer {

  private final Tracer delegate;

  /** Create a new {@link ExtendedTracer} that wraps the provided Tracer. */
  public static ExtendedTracer create(Tracer delegate) {
    return new ExtendedTracer(delegate);
  }

  private ExtendedTracer(Tracer delegate) {
    this.delegate = delegate;
  }

  /** Run the provided {@link Runnable} and wrap with a {@link Span} with the provided name. */
  public void run(String spanName, Runnable runnable) {
    Span span = delegate.spanBuilder(spanName).startSpan();
    try (Scope scope = span.makeCurrent()) {
      runnable.run();
    } catch (Throwable e) {
      span.recordException(e);
      throw e;
    } finally {
      span.end();
    }
  }

  /** Call the provided {@link Callable} and wrap with a {@link Span} with the provided name. */
  public <T> T call(String spanName, Callable<T> callable) throws Exception {
    Span span = delegate.spanBuilder(spanName).startSpan();
    try (Scope scope = span.makeCurrent()) {
      return callable.call();
    } catch (Throwable e) {
      span.recordException(e);
      throw e;
    } finally {
      span.end();
    }
  }

  @Override
  public SpanBuilder spanBuilder(String spanName) {
    return delegate.spanBuilder(spanName);
  }
}
