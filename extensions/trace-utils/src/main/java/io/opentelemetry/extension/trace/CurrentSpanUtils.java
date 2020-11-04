/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.concurrent.Callable;

/** Util methods/functionality to interact with the {@link Span} in the {@link Context}. */
public final class CurrentSpanUtils {
  // No instance of this class.
  private CurrentSpanUtils() {}

  /**
   * Wraps a {@link Runnable} so that it executes with the {@code span} as the current {@code Span}.
   *
   * @param span the {@code Span} to be set as current.
   * @param endSpan if {@code true} the returned {@code Runnable} will close the {@code Span}.
   * @param runnable the {@code Runnable} to run in the {@code Span}.
   * @return the wrapped {@code Runnable}.
   */
  public static Runnable withSpan(Span span, boolean endSpan, Runnable runnable) {
    return new RunnableInSpan(span, runnable, endSpan);
  }

  /**
   * Wraps a {@link Callable} so that it executes with the {@code span} as the current {@code Span}.
   *
   * @param span the {@code Span} to be set as current.
   * @param endSpan if {@code true} the returned {@code Runnable} will close the {@code Span}.
   * @param callable the {@code Callable} to run in the {@code Span}.
   * @param <C> the {@code Callable} result type.
   * @return the wrapped {@code Callable}.
   */
  public static <C> Callable<C> withSpan(Span span, boolean endSpan, Callable<C> callable) {
    return new CallableInSpan<C>(span, callable, endSpan);
  }

  private static final class RunnableInSpan implements Runnable {
    private final Span span;
    private final Runnable runnable;
    private final boolean endSpan;

    private RunnableInSpan(Span span, Runnable runnable, boolean endSpan) {
      this.span = span;
      this.runnable = runnable;
      this.endSpan = endSpan;
    }

    @Override
    public void run() {
      try (Scope ignored = Context.current().with(span).makeCurrent()) {
        runnable.run();
      } catch (Throwable t) {
        setErrorStatus(span, t);
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        } else if (t instanceof Error) {
          throw (Error) t;
        }
        throw new IllegalStateException("unexpected", t);
      } finally {
        if (endSpan) {
          span.end();
        }
      }
    }
  }

  private static final class CallableInSpan<V> implements Callable<V> {
    private final Span span;
    private final Callable<V> callable;
    private final boolean endSpan;

    private CallableInSpan(Span span, Callable<V> callable, boolean endSpan) {
      this.span = span;
      this.callable = callable;
      this.endSpan = endSpan;
    }

    @Override
    public V call() throws Exception {
      try (Scope ignored = Context.current().with(span).makeCurrent()) {
        return callable.call();
      } catch (Exception e) {
        setErrorStatus(span, e);
        throw e;
      } catch (Throwable t) {
        setErrorStatus(span, t);
        if (t instanceof Error) {
          throw (Error) t;
        }
        throw new IllegalStateException("unexpected", t);
      } finally {
        if (endSpan) {
          span.end();
        }
      }
    }
  }

  private static void setErrorStatus(Span span, Throwable t) {
    span.setStatus(
        StatusCode.ERROR, t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
  }
}
