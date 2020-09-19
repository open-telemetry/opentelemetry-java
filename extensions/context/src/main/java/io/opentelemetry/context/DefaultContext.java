/*
 * Copyright The OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentelemetry.context;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nullable;

final class DefaultContext implements Context {

  static final Context ROOT = new DefaultContext();

  /**
   * Returns the default {@link ContextStorage} used to attach {@link DefaultContext}s to scopes of
   * execution. Should only be used when defining your own {@link ContextStorage} in case you want
   * to delegate functionality to the default implementation.
   */
  public static ContextStorage threadLocalStorage() {
    return ThreadLocalContextStorage.INSTANCE;
  }

  private final PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> entries;

  private DefaultContext(PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> entries) {
    this.entries = entries;
  }

  DefaultContext() {
    entries = null;
  }

  @Override
  @Nullable
  public <V> V getValue(ContextKey<V> key) {
    // Because withValue enforces the value for a key is its type, this is always safe.
    @SuppressWarnings("unchecked")
    V value = (V) PersistentHashArrayMappedTrie.get(entries, key);
    return value;
  }

  @Override
  public <V> Context withValue(ContextKey<V> k1, V v1) {
    PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> newEntries =
        PersistentHashArrayMappedTrie.put(entries, k1, v1);
    return new DefaultContext(newEntries);
  }

  @Override
  public <V1, V2> Context withValues(ContextKey<V1> k1, V1 v1, ContextKey<V2> k2, V2 v2) {
    PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> newEntries =
        PersistentHashArrayMappedTrie.put(entries, k1, v1);
    newEntries = PersistentHashArrayMappedTrie.put(newEntries, k2, v2);
    return new DefaultContext(newEntries);
  }

  @Override
  public <V1, V2, V3> Context withValues(
      ContextKey<V1> k1, V1 v1, ContextKey<V2> k2, V2 v2, ContextKey<V3> k3, V3 v3) {
    PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> newEntries =
        PersistentHashArrayMappedTrie.put(entries, k1, v1);
    newEntries = PersistentHashArrayMappedTrie.put(newEntries, k2, v2);
    newEntries = PersistentHashArrayMappedTrie.put(newEntries, k3, v3);
    return new DefaultContext(newEntries);
  }

  @Override
  public <V1, V2, V3, V4> Context withValues(
      ContextKey<V1> k1,
      V1 v1,
      ContextKey<V2> k2,
      V2 v2,
      ContextKey<V3> k3,
      V3 v3,
      ContextKey<V4> k4,
      V4 v4) {
    PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> newEntries =
        PersistentHashArrayMappedTrie.put(entries, k1, v1);
    newEntries = PersistentHashArrayMappedTrie.put(newEntries, k2, v2);
    newEntries = PersistentHashArrayMappedTrie.put(newEntries, k3, v3);
    newEntries = PersistentHashArrayMappedTrie.put(newEntries, k4, v4);
    return new DefaultContext(newEntries);
  }

  @Override
  public Scope attach() {
    final Context thisCtx = this;
    final Context prevCtx = LazyStorage.get().attach(this);

    if (thisCtx == prevCtx) {
      // Already attached, so just creating a new scope that doesn't do anything.
      return NoopScope.INSTANCE;
    }

    return new Scope() {
      @Override
      public void close() {
        LazyStorage.get().detach(thisCtx, prevCtx);
      }
    };
  }

  @Override
  public Runnable wrap(final Runnable runnable) {
    return new Runnable() {
      @Override
      public void run() {
        try (Scope ignored = attach()) {
          runnable.run();
        }
      }
    };
  }

  @Override
  public <T> Callable<T> wrap(final Callable<T> callable) {
    return new Callable<T>() {
      @Override
      public T call() throws Exception {
        try (Scope ignored = attach()) {
          return callable.call();
        }
      }
    };
  }

  @Override
  public Executor wrap(final Executor executor) {
    return new Executor() {
      @Override
      public void execute(Runnable command) {
        executor.execute(wrap(command));
      }
    };
  }

  @Override
  public ExecutorService wrap(ExecutorService executor) {
    return new ContextExecutorService(this, executor);
  }

  @Override
  public ScheduledExecutorService wrap(ScheduledExecutorService executor) {
    return new ContextScheduledExecutorService(this, executor);
  }

  private enum NoopScope implements Scope {
    INSTANCE;

    @Override
    public void close() {}
  }
}
