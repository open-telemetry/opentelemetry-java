/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import javax.annotation.Nullable;

/**
 * A read-only container for String-keyed attributes.
 *
 * @param <V> The type of the values contained in this.
 */
public interface ReadableKeyValuePairs<V> {
  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(KeyValueConsumer<V> consumer);

  /**
   * Returns the value of the given key, or null if the key does not exist.
   *
   * <p>Currently may be implemented via a linear search, depending on implementation, so O(n)
   * performance in the worst case.
   */
  @Nullable
  V get(String key);

  /**
   * Used for iterating over the key-value pairs in a key-value pair container, such as {@link
   * Attributes} or {@link Labels}. The key is always a {@link String}.
   *
   * @param <V> The type of the values contained in the key-value pairs.
   */
  interface KeyValueConsumer<V> {
    void consume(String key, V value);
  }
}
