/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.StatusCanonicalCode;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link CurrentSpanUtils}. */
class CurrentSpanUtilsTest {
  @Mock private Span span;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

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
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
        };
    CurrentSpanUtils.withSpan(span, false, runnable).run();
    verifyNoInteractions(span);
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanRunnable_EndSpan() {
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
        };
    CurrentSpanUtils.withSpan(span, true, runnable).run();
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanRunnable_WithError() {
    final AssertionError error = new AssertionError("MyError");
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeRunnableAndExpectError(runnable, error);
    verify(span).setStatus(StatusCanonicalCode.ERROR, "MyError");
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanRunnable_WithErrorNoMessage() {
    final AssertionError error = new AssertionError();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Runnable runnable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeRunnableAndExpectError(runnable, error);
    verify(span).setStatus(StatusCanonicalCode.ERROR, "AssertionError");
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanCallable() throws Exception {
    final Object ret = new Object();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          return ret;
        };
    assertThat(CurrentSpanUtils.withSpan(span, false, callable).call()).isEqualTo(ret);
    verifyNoInteractions(span);
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanCallable_EndSpan() throws Exception {
    final Object ret = new Object();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          return ret;
        };
    assertThat(CurrentSpanUtils.withSpan(span, true, callable).call()).isEqualTo(ret);
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanCallable_WithException() {
    final Exception exception = new Exception("MyException");
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw exception;
        };
    executeCallableAndExpectError(callable, exception);
    verify(span).setStatus(StatusCanonicalCode.ERROR, "MyException");
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanCallable_WithExceptionNoMessage() {
    final Exception exception = new Exception();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw exception;
        };
    executeCallableAndExpectError(callable, exception);
    verify(span).setStatus(StatusCanonicalCode.ERROR, "Exception");
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanCallable_WithError() {
    final AssertionError error = new AssertionError("MyError");
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeCallableAndExpectError(callable, error);
    verify(span).setStatus(StatusCanonicalCode.ERROR, "MyError");
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpanCallable_WithErrorNoMessage() {
    final AssertionError error = new AssertionError();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Callable<Object> callable =
        () -> {
          // When we run the runnable we will have the span in the current Context.
          assertThat(getCurrentSpan()).isSameAs(span);
          throw error;
        };
    executeCallableAndExpectError(callable, error);
    verify(span).setStatus(StatusCanonicalCode.ERROR, "AssertionError");
    verify(span).end();
    assertThat(getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  private static Span getCurrentSpan() {
    return TracingContextUtils.getCurrentSpan();
  }
}
