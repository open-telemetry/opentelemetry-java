/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentSpanUtilsTest {
  @Spy private Span span;

  // TODO(bdrutu): When update to junit 4.13 use assertThrows instead of this.
  private void executeRunnableAndExpectError(Runnable runnable, Throwable error) {
    boolean called = false;
    try {
      CurrentSpanUtils.withSpan(span, true, runnable).run();
    } catch (Throwable e) {
      assertThat(e).isEqualTo(error);
      called = true;
    }
    assertThat(called).isTrue();
  }

  // TODO(bdrutu): When update to junit 4.13 use assertThrows instead of this.
  private void executeCallableAndExpectError(Callable<Object> callable, Throwable error) {
    boolean called = false;
    try {
      CurrentSpanUtils.withSpan(span, true, callable).call();
    } catch (Throwable e) {
      assertThat(e).isEqualTo(error);
      called = true;
    }
    assertThat(called).isTrue();
  }

  @Test
  void withSpanRunnable() {
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
        };
    CurrentSpanUtils.withSpan(span, false, runnable).run();
    verify(span, never()).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanRunnable_EndSpan() {
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
        };
    CurrentSpanUtils.withSpan(span, true, runnable).run();
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanRunnable_WithError() {
    final AssertionError error = new AssertionError("MyError");
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeRunnableAndExpectError(runnable, error);
    verify(span).setStatus(StatusCode.ERROR, "MyError");
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanRunnable_WithErrorNoMessage() {
    final AssertionError error = new AssertionError();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeRunnableAndExpectError(runnable, error);
    verify(span).setStatus(StatusCode.ERROR, "AssertionError");
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanCallable() throws Exception {
    final Object ret = new Object();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          return ret;
        };
    assertThat(CurrentSpanUtils.withSpan(span, false, callable).call()).isEqualTo(ret);
    verify(span, never()).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanCallable_EndSpan() throws Exception {
    final Object ret = new Object();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          return ret;
        };
    assertThat(CurrentSpanUtils.withSpan(span, true, callable).call()).isEqualTo(ret);
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanCallable_WithException() {
    final Exception exception = new Exception("MyException");
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw exception;
        };
    executeCallableAndExpectError(callable, exception);
    verify(span).setStatus(StatusCode.ERROR, "MyException");
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanCallable_WithExceptionNoMessage() {
    final Exception exception = new Exception();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw exception;
        };
    executeCallableAndExpectError(callable, exception);
    verify(span).setStatus(StatusCode.ERROR, "Exception");
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanCallable_WithError() {
    final AssertionError error = new AssertionError("MyError");
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeCallableAndExpectError(callable, error);
    verify(span).setStatus(StatusCode.ERROR, "MyError");
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void withSpanCallable_WithErrorNoMessage() {
    final AssertionError error = new AssertionError();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeCallableAndExpectError(callable, error);
    verify(span).setStatus(StatusCode.ERROR, "AssertionError");
    verify(span).end();
    assertThat(getCurrentSpan().getSpanContext().isValid()).isFalse();
  }

  private static Span getCurrentSpan() {
    return Span.current();
  }
}
