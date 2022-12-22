/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

enum ThreadLocalContextStorage implements ContextStorage {
  INSTANCE;

  private static final Logger logger = Logger.getLogger(ThreadLocalContextStorage.class.getName());

  private static final ThreadLocal<Context> THREAD_LOCAL_STORAGE = new ThreadLocal<>();

  @Override
  public Scope attach(Context toAttach) {
    if (toAttach == null) {
      // Null context not allowed so ignore it.
      return NoopScope.INSTANCE;
    }

    Context beforeAttach = current();
    if (toAttach == beforeAttach) {
      return NoopScope.INSTANCE;
    }

    THREAD_LOCAL_STORAGE.set(toAttach);

    return new ScopeImpl(beforeAttach, toAttach);
  }

  private class ScopeImpl implements Scope {
    @Nullable private final Context beforeAttach;
    private final Context toAttach;
    private boolean closed;

    private ScopeImpl(@Nullable Context beforeAttach, Context toAttach) {
      this.beforeAttach = beforeAttach;
      this.toAttach = toAttach;
    }

    @Override
    public void close() {
      if (!closed && current() == toAttach) {
        closed = true;
        THREAD_LOCAL_STORAGE.set(beforeAttach);
      } else {
        logger.log(
            Level.FINE,
            " Trying to close scope which does not represent current context. Ignoring the call.");
      }
    }
  }

  @Override
  @Nullable
  public Context current() {
    return THREAD_LOCAL_STORAGE.get();
  }

  enum NoopScope implements Scope {
    INSTANCE;

    @Override
    public void close() {}
  }
}
