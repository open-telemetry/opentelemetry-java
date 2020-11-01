/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

enum ThreadLocalContextStorage implements ContextStorage {
  INSTANCE;

  private static final Logger logger = Logger.getLogger(ThreadLocalContextStorage.class.getName());

  private static final ThreadLocal<Context> THREAD_LOCAL_STORAGE = ThreadLocal.withInitial(
      Context::root);

  private static final AtomicReference<Consumer<Context>> onAttachConsumer = new AtomicReference<>();

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

    onAttach(toAttach);
    THREAD_LOCAL_STORAGE.set(toAttach);

    return () -> {
      if (current() != toAttach) {
        logger.log(
            Level.FINE,
            "Context in storage not the expected context, Scope.close was not called correctly");
      }
      onAttach(beforeAttach);
      THREAD_LOCAL_STORAGE.set(beforeAttach);
    };
  }

  private void onAttach(Context toAttach) {
    Consumer<Context> consumer = onAttachConsumer.get();
    if (consumer != null) {
      consumer.accept(toAttach);
    }
  }

  @Override
  public Context current() {
    return THREAD_LOCAL_STORAGE.get();
  }

  @Override
  public void onAttach(Consumer<Context> contextConsumer) {
    onAttachConsumer.updateAndGet(existing -> existing != null ? existing.andThen(contextConsumer) : contextConsumer);
  }

  enum NoopScope implements Scope {
    INSTANCE;

    @Override
    public void close() {}
  }
}
