/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.trace;

import io.grpc.Context;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.concurrent.Callable;

/** Util methods/functionality to interact with the {@link Span} in the {@link io.grpc.Context}. */
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
      Context origContext = TracingContextUtils.withSpan(span, Context.current()).attach();
      try {
        runnable.run();
      } catch (Throwable t) {
        setErrorStatus(span, t);
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        } else if (t instanceof Error) {
          throw (Error) t;
        }
        throw new RuntimeException("unexpected", t);
      } finally {
        Context.current().detach(origContext);
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
      Context origContext = TracingContextUtils.withSpan(span, Context.current()).attach();
      try {
        return callable.call();
      } catch (Exception e) {
        setErrorStatus(span, e);
        throw e;
      } catch (Throwable t) {
        setErrorStatus(span, t);
        if (t instanceof Error) {
          throw (Error) t;
        }
        throw new RuntimeException("unexpected", t);
      } finally {
        Context.current().detach(origContext);
        if (endSpan) {
          span.end();
        }
      }
    }
  }

  private static void setErrorStatus(Span span, Throwable t) {
    span.setStatus(
        Status.UNKNOWN.withDescription(
            t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
  }
}
