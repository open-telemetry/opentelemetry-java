/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.noopapi;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import io.opentelemetry.context.Scope;
import javax.annotation.Nullable;

/**
 * A {@link ContextStorageProvider} that returns a {@link ContextStorage} which is completely no-op.
 */
public class NoopContextStorageProvider implements ContextStorageProvider {
  @Override
  public ContextStorage get() {
    return NoopContextStorage.INSTANCE;
  }

  enum NoopContextStorage implements ContextStorage {
    INSTANCE;

    @Override
    public Scope attach(Context toAttach) {
      return Scope.noop();
    }

    @Override
    public Context current() {
      return NoopContext.INSTANCE;
    }
  }

  enum NoopContext implements Context {
    INSTANCE;

    @Nullable
    @Override
    public <V> V get(ContextKey<V> key) {
      return null;
    }

    @Override
    public <V> Context with(ContextKey<V> k1, V v1) {
      return this;
    }
  }
}
