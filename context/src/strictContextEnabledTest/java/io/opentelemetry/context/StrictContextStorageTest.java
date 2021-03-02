/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2013-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@SuppressWarnings("MustBeClosedChecker")
class StrictContextStorageTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");
  private static final String TRACE_ID = "7b2e170db4df2d593ddb4ddf2ddf2d59";
  private static final String SPAN_ID = "b2e170db4df2d593";

  private static final Span SPAN =
      Span.wrap(
          SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(StrictContextStorage.class);

  // In this test we intentionally leak context so need to restore it ourselves, bypassing the
  // strict storage.
  @AfterEach
  void resetContext() {
    ThreadLocalContextStorage.INSTANCE.attach(Context.root());
  }

  // TODO(anuraaga): These rules conflict with error prone so one or the other needs to be
  // disabled.
  @SuppressWarnings({"checkstyle:EmptyBlock", "checkstyle:WhitespaceAround"})
  @Test
  void decorator_close_afterCorrectUsage() {
    try (Scope ws = Context.current().with(ANIMAL, "cat").makeCurrent()) {
      try (Scope ws2 = Context.current().with(ANIMAL, "dog").makeCurrent()) {}
    }

    ((StrictContextStorage) ContextStorage.get()).close(); // doesn't error
  }

  static final class BusinessClass {

    static Scope businessMethodMakeContextCurrent() {
      return Context.current().with(ANIMAL, "cat").makeCurrent();
    }

    static Scope businessMethodMakeSpanCurrent() {
      return SPAN.makeCurrent();
    }

    private BusinessClass() {}
  }

  @Test
  public void scope_close_onWrongThread_newScope() throws Exception {
    scope_close_onWrongThread(
        BusinessClass::businessMethodMakeContextCurrent, "businessMethodMakeContextCurrent");
  }

  @Test
  public void decorator_close_withLeakedScope_onWrongThread_newScope() throws Exception {
    decorator_close_withLeakedScope(
        BusinessClass::businessMethodMakeContextCurrent, "businessMethodMakeContextCurrent");
  }

  @Test
  public void scope_close_onWrongThread_withSpanInScope() throws Exception {
    scope_close_onWrongThread(
        BusinessClass::businessMethodMakeSpanCurrent, "businessMethodMakeSpanCurrent");
  }

  @Test
  public void decorator_close_withLeakedScope_withSpanInScope() throws Exception {
    decorator_close_withLeakedScope(
        BusinessClass::businessMethodMakeSpanCurrent, "businessMethodMakeSpanCurrent");
  }

  void scope_close_onWrongThread(Supplier<Scope> method, String methodName) throws Exception {
    AtomicReference<Scope> closeable = new AtomicReference<>();
    Thread t1 = new Thread(() -> closeable.set(method.get()));
    t1.setName("t1");
    t1.start();
    t1.join();

    AtomicReference<Throwable> errorCatcher = new AtomicReference<>();

    Thread t2 =
        new Thread(
            () -> {
              try {
                closeable.get().close();
              } catch (Throwable t) {
                errorCatcher.set(t);
              }
            });
    t2.setName("t2");
    t2.start();
    t2.join();

    assertThat(errorCatcher.get())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Thread [t1] opened scope, but thread [t2] closed it")
        .satisfies(
            e ->
                assertThat(e.getCause().getMessage())
                    .matches("Thread \\[t1\\] opened scope for .* here:"));
  }

  @SuppressWarnings("ReturnValueIgnored")
  void decorator_close_withLeakedScope(Supplier<Scope> method, String methodName) throws Exception {
    Thread thread = new Thread(method::get);
    thread.setName("t1");
    thread.start();
    thread.join();

    assertThatThrownBy(() -> ((StrictContextStorage) ContextStorage.get()).close())
        .isInstanceOf(AssertionError.class)
        .satisfies(
            t -> assertThat(t.getMessage()).matches("Thread \\[t1\\] opened a scope of .* here:"))
        .hasNoCause();
  }

  static void assertStackTraceStartsWithMethod(Throwable throwable, String methodName) {
    assertThat(throwable.getStackTrace()[0].getMethodName()).isEqualTo(methodName);
  }

  @Test
  @SuppressWarnings("UnusedVariable")
  void multipleLeaks() {
    Scope scope1 = Context.current().with(ANIMAL, "cat").makeCurrent();
    Scope scope2 = Context.current().with(ANIMAL, "dog").makeCurrent();
    assertThatThrownBy(() -> ((StrictContextStorage) ContextStorage.get()).close())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void garbageCollectedScope() {
    Context.current().with(ANIMAL, "cat").makeCurrent();

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(
            () -> {
              System.gc();
              LoggingEvent log =
                  logs.assertContains("Scope garbage collected before being closed.");
              assertThat(log.getLevel()).isEqualTo(Level.ERROR);
              assertThat(log.getThrowable().getMessage())
                  .matches("Thread \\[Test worker\\] opened a scope of .* here:");
            });
  }
}
