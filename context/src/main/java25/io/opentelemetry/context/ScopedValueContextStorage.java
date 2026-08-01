/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A {@link ContextStorage} for Java 25+ that uses {@link ScopedValueContext} as the {@link
 * Context} implementation.
 *
 * <p>Two storage paths exist side by side:
 *
 * <ul>
 *   <li><b>{@link ScopedValue} path</b> — established by {@link Context#wrap(Runnable)} and
 *       friends. Propagates automatically to virtual thread children. Zero per-thread allocation.
 *   <li><b>{@link ThreadLocal} path</b> — established by {@link Context#makeCurrent()}. Preserves
 *       the existing open-scope {@code attach}/{@code close} contract for callers that rely on it.
 * </ul>
 *
 * <p>{@link #current()} checks the {@link ThreadLocal} first, then falls back to the {@link
 * ScopedValue}. This means a {@link Context#makeCurrent()} call inside a {@link
 * Context#wrap(Runnable)} scope correctly shadows the inherited value for the current thread while
 * leaving the ScopedValue binding intact for any virtual thread children spawned concurrently.
 */
public class ScopedValueContextStorage implements ContextStorage {

  // TODO: use ScopedValueContextStorage.class.getName() once ContextTest's LogCapturer is updated
  // to capture from both storage implementations. For now, share the ThreadLocalContextStorage
  // logger name so tests and log configuration targeting that name work regardless of which
  // storage is active.
  private static final Logger logger =
      Logger.getLogger(ThreadLocalContextStorage.class.getName());

  // Package-private so ScopedValueContext.wrap() can suspend/restore it.
  static final ThreadLocal<Context> THREAD_LOCAL = new ThreadLocal<>();

  private static final ScopedValueContextStorage INSTANCE = new ScopedValueContextStorage();

  private ScopedValueContextStorage() {}

  public static ContextStorage getInstance() {
    return INSTANCE;
  }

  @Override
  public Scope attach(Context toAttach) {
    if (toAttach == null) {
      return Scope.noop();
    }

    Context before = current();
    if (toAttach == before) {
      return Scope.noop();
    }

    // Capture the raw ThreadLocal value — may be null if the current context came from SCOPED.
    // We restore exactly this on close rather than the logical "before" so that when we exit a
    // makeCurrent() scope that was entered inside a wrap() scope, current() correctly falls back
    // to the ScopedValue binding instead of holding a stale ThreadLocal reference.
    Context threadLocalBefore = THREAD_LOCAL.get();
    THREAD_LOCAL.set(toAttach);
    return new ScopeImpl(threadLocalBefore, toAttach);
  }

  @Override
  @Nullable
  public Context current() {
    // makeCurrent() path takes priority within this thread.
    Context local = THREAD_LOCAL.get();
    if (local != null) {
      return local;
    }
    // Fall back to the ScopedValue binding, which may have been established by wrap() on this
    // thread or inherited from a parent virtual thread.
    return ScopedValueContext.SCOPED.isBound() ? ScopedValueContext.SCOPED.get() : null;
  }

  @Override
  public Context root() {
    return ScopedValueContext.root();
  }

  private class ScopeImpl implements Scope {
    @Nullable private final Context threadLocalBefore;
    private final Context toAttach;
    private boolean closed;

    private ScopeImpl(@Nullable Context threadLocalBefore, Context toAttach) {
      this.threadLocalBefore = threadLocalBefore;
      this.toAttach = toAttach;
    }

    @Override
    public void close() {
      if (!closed && current() == toAttach) {
        closed = true;
        if (threadLocalBefore == null) {
          THREAD_LOCAL.remove();
        } else {
          THREAD_LOCAL.set(threadLocalBefore);
        }
      } else {
        logger.log(
            Level.FINE,
            "Trying to close scope which does not represent current context. Ignoring the call.");
      }
    }
  }
}
