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

package io.opentelemetry.sdk.testing.context;

import static java.lang.Thread.currentThread;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
public class StrictContextStorage implements ContextStorage {

  /**
   * Returns a new {@link StrictContextStorage} which delegates to the provided {@link
   * ContextStorage}, wrapping created scopes to track their usage.
   */
  public static StrictContextStorage create(ContextStorage delegate) {
    return new StrictContextStorage(delegate);
  }

  // Visible for testing
  static final Logger logger = Logger.getLogger(StrictContextStorage.class.getName());

  private final ContextStorage delegate;
  private final PendingScopes pendingScopes;

  private StrictContextStorage(ContextStorage delegate) {
    this.delegate = delegate;
    pendingScopes = PendingScopes.create();
  }

  @Override
  public Scope attach(Context context) {
    Scope scope = delegate.attach(context);

    CallerStackTrace caller = new CallerStackTrace(context);
    StackTraceElement[] stackTrace = caller.getStackTrace();

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

  /**
   * Ensures all scopes that have been created by this storage have been closed. This can be useful
   * to call at the end of a test to make sure everything has been cleaned up.
   *
   * <p><em>Note:</em> It is important to close all resources prior to calling this, so that
   * in-flight operations are not mistaken as scope leaks. If this raises an error, consider if a
   * {@linkplain Context#wrap(Executor)} wrapped executor} is still running.
   *
   * @throws AssertionError if any scopes were left unclosed.
   */
  // AssertionError to ensure test runners render the stack trace
  public void ensureAllClosed() {
    pendingScopes.expungeStaleEntries();
    List<CallerStackTrace> leaked = pendingScopes.drainPendingCallers();
    if (!leaked.isEmpty()) {
      if (leaked.size() > 1) {
        logger.log(Level.SEVERE, "Multiple scopes leaked - first will be thrown as an error.");
      }
      for (CallerStackTrace caller : leaked) {
        logger.log(Level.SEVERE, "Scope leaked", callerError(caller));
      }
      throw callerError(leaked.get(0));
    }
  }

  final class StrictScope implements Scope {
    final Scope delegate;
    final CallerStackTrace caller;

    StrictScope(Scope delegate, CallerStackTrace caller) {
      this.delegate = delegate;
      this.caller = caller;
      pendingScopes.put(this, caller);
    }

    @Override
    public void close() {
      caller.closed = true;
      pendingScopes.remove(this);

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

    volatile boolean closed;

    CallerStackTrace(Context context) {
      super("Thread [" + currentThread().getName() + "] opened scope for " + context + " here:");
      this.context = context;
    }
  }

  static class PendingScopes extends WeakConcurrentMap<Scope, CallerStackTrace> {

    static PendingScopes create() {
      return new PendingScopes(new ConcurrentHashMap<>());
    }

    // We need to explicitly pass a map to the constructor because we otherwise cannot remove from
    // it. https://github.com/raphw/weak-lock-free/pull/12
    private final ConcurrentHashMap<WeakKey<Scope>, CallerStackTrace> map;

    PendingScopes(ConcurrentHashMap<WeakKey<Scope>, CallerStackTrace> map) {
      super(/* cleanerThread= */ true, /* reuseKeys= */ false, map);
      this.map = map;
    }

    List<CallerStackTrace> drainPendingCallers() {
      List<CallerStackTrace> pendingCallers =
          map.values().stream().filter(caller -> !caller.closed).collect(Collectors.toList());
      map.clear();
      return pendingCallers;
    }

    // Called by cleaner thread.
    @Override
    public void run() {
      try {
        while (!Thread.interrupted()) {
          CallerStackTrace caller = map.remove(remove());
          if (caller != null && !caller.closed) {
            logger.log(
                Level.SEVERE, "Scope garbage collected before being closed.", callerError(caller));
          }
        }
      } catch (InterruptedException ignored) {
        // do nothing
      }
    }
  }

  static AssertionError callerError(CallerStackTrace caller) {
    // Sometimes unit test runners truncate the cause of the exception.
    // This flattens the exception as the caller of close() isn't important vs the one that leaked
    AssertionError toThrow =
        new AssertionError(
            "Thread [" + caller.threadName + "] opened a scope of " + caller.context + " here:");
    toThrow.setStackTrace(caller.getStackTrace());
    return toThrow;
  }
}
