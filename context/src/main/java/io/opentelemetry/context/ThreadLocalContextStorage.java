/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum ThreadLocalContextStorage implements ContextStorage {
  INSTANCE;

  private static final Logger logger = Logger.getLogger(ThreadLocalContextStorage.class.getName());

  private static final ThreadLocal<Context> THREAD_LOCAL_STORAGE = new ThreadLocal<>();

  static {
    THREAD_LOCAL_STORAGE.set(Context.root());
  }

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

    return () -> {
      if (current() != toAttach) {
        logger.log(
            Level.FINE,
            "Context in storage not the expected context, Scope.close was not called correctly");
      }
      THREAD_LOCAL_STORAGE.set(beforeAttach);
    };
  }

  @Override
  public Context current() {
    return THREAD_LOCAL_STORAGE.get();
  }

  enum NoopScope implements Scope {
    INSTANCE;

    @Override
    public void close() {}
  }
}
