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

import static java.lang.Thread.currentThread;

import java.util.Arrays;

/**
 * A {@link ContextStorage} which keeps track of opened and closed {@link Scope}s, reporting caller
 * information if a {@link Scope} is closed incorrectly or not at all. This is useful in
 * instrumentation tests to check whether any scopes leaked.
 *
 * <pre>{@code
 * > class MyInstrumentationTest {
 * >   private static ContextStorage previousStorage;
 * >   private static StrictContextStorage strictStorage;
 * >
 * >   @BeforeAll
 * >   static void setUp() {
 * >     previousStorage = ContextStorage.get()
 * >     strictStorage = StrictContextStorage.create(previousStorage);
 * >     ContextStorage.set(strictStorage);
 * >   }
 * >
 * >   @AfterEach
 * >   void checkScopes() {
 * >     strictStorage.ensureAllClosed();
 * >   }
 * >
 * >   @AfterAll
 * >   static void tearDown() {
 * >     ContextStorage.set(previousStorage);
 * >   }
 * >
 * >   @Test
 * >   void badTest() {
 * >     Context.root().makeCurrent();
 * >   }
 * > }
 * }</pre>
 */
final class StrictContextStorage implements ContextStorage {

  /**
   * Returns a new {@link StrictContextStorage} which delegates to the provided {@link
   * ContextStorage}, wrapping created scopes to track their usage.
   */
  static StrictContextStorage create(ContextStorage delegate) {
    return new StrictContextStorage(delegate);
  }

  private final ContextStorage delegate;

  private StrictContextStorage(ContextStorage delegate) {
    this.delegate = delegate;
  }

  @Override
  public Scope attach(Context context) {
    Scope scope = delegate.attach(context);

    CallerStackTrace caller = new CallerStackTrace(context);
    StackTraceElement[] stackTrace = caller.getStackTrace();

    // Detect invalid use from top-level kotlin coroutine. The stacktrace will have the order
    // makeCurrent -> invokeSuspend -> resumeWith
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement element = stackTrace[i];
      if (element.getClassName().equals(Context.class.getName())
          && element.getMethodName().equals("makeCurrent")) {
        if (i + 2 < stackTrace.length) {
          StackTraceElement maybeResumptionElement = stackTrace[i + 2];
          if (maybeResumptionElement
                  .getClassName()
                  .equals("kotlin.coroutines.jvm.internal.BaseContinuationImpl")
              && maybeResumptionElement.getMethodName().equals("resumeWith")) {
            throw new AssertionError(
                "Attempting to call Context.makeCurrent from inside a Kotlin coroutine. "
                    + "This is not allowed. Use Context.asContextElement provided by "
                    + "opentelemetry-extension-kotlin instead of makeCurrent.");
          }
        }
      }
    }

    // "new CallerStackTrace(context)" isn't the line we want to start the caller stack trace with
    int i = 1;

    // This skips OpenTelemetry API and Context packages which will be at the top of the stack
    // trace above the business logic call.
    while (i < stackTrace.length) {
      String className = stackTrace[i].getClassName();
      if (className.startsWith("io.opentelemetry.api.")
          || className.startsWith(
              "io.opentelemetry.sdk.testing.context.SettableContextStorageProvider")
          || className.startsWith("io.opentelemetry.context.")) {
        i++;
      } else {
        break;
      }
    }
    int from = i;

    stackTrace = Arrays.copyOfRange(stackTrace, from, stackTrace.length);
    caller.setStackTrace(stackTrace);

    return new StrictScope(scope, caller);
  }

  @Override
  public Context current() {
    return delegate.current();
  }

  static final class StrictScope implements Scope {
    final Scope delegate;
    final CallerStackTrace caller;

    StrictScope(Scope delegate, CallerStackTrace caller) {
      this.delegate = delegate;
      this.caller = caller;
    }

    @Override
    public void close() {
      // Detect invalid use from Kotlin suspending function. For non top-level coroutines, we can
      // only detect illegal usage on close, which will happen after the suspending function
      // resumes and is decoupled from the caller.
      // Illegal usage is close -> (optional closeFinally) -> "suspending function name" ->
      // resumeWith.
      StackTraceElement[] stackTrace = new Throwable().getStackTrace();
      for (int i = 0; i < stackTrace.length; i++) {
        StackTraceElement element = stackTrace[i];
        if (element.getClassName().equals(StrictScope.class.getName())
            && element.getMethodName().equals("close")) {
          int maybeResumeWithFrameIndex = i + 2;
          if (i + 1 < stackTrace.length) {
            StackTraceElement nextElement = stackTrace[i + 1];
            if (nextElement.getClassName().equals("kotlin.jdk7.AutoCloseableKt")
                && nextElement.getMethodName().equals("closeFinally")
                && i + 2 < stackTrace.length) {
              // Skip extension method for AutoCloseable.use
              maybeResumeWithFrameIndex = i + 3;
            }
          }
          if (stackTrace[maybeResumeWithFrameIndex].getMethodName().equals("invokeSuspend")) {
            // Skip synthetic invokeSuspend function.
            // NB: The stacktrace showed in an IntelliJ debug pane does not show this.
            maybeResumeWithFrameIndex++;
          }
          if (maybeResumeWithFrameIndex < stackTrace.length) {
            StackTraceElement maybeResumptionElement = stackTrace[maybeResumeWithFrameIndex];
            if (maybeResumptionElement
                    .getClassName()
                    .equals("kotlin.coroutines.jvm.internal.BaseContinuationImpl")
                && maybeResumptionElement.getMethodName().equals("resumeWith")) {
              throw new AssertionError(
                  "Attempting to close a Scope created by Context.makeCurrent from inside a Kotlin "
                      + "coroutine. This is not allowed. Use Context.asContextElement provided by "
                      + "opentelemetry-extension-kotlin instead of makeCurrent.");
            }
          }
        }
      }

      if (currentThread().getId() != caller.threadId) {
        throw new IllegalStateException(
            String.format(
                "Thread [%s] opened scope, but thread [%s] closed it",
                caller.threadName, currentThread().getName()),
            caller);
      }
      delegate.close();
    }

    @Override
    public String toString() {
      return caller.getMessage();
    }
  }

  static class CallerStackTrace extends Throwable {

    private static final long serialVersionUID = 783294061323215387L;

    final String threadName = currentThread().getName();
    final long threadId = currentThread().getId();
    final Context context;

    CallerStackTrace(Context context) {
      super("Thread [" + currentThread().getName() + "] opened scope for " + context + " here:");
      this.context = context;
    }
  }
}
