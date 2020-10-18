/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import javax.annotation.Nullable;

final class DefaultContext implements Context {

  static final Context ROOT = new DefaultContext();

  /**
   * Returns the default {@link ContextStorage} used to attach {@link Context}s to scopes of
   * execution. Should only be used when defining your own {@link ContextStorage} in case you want
   * to delegate functionality to the default implementation.
   */
  static ContextStorage threadLocalStorage() {
    return ThreadLocalContextStorage.INSTANCE;
  }

  @Nullable private final PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> entries;

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
  public <V> Context withValues(ContextKey<V> k1, V v1) {
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
}
