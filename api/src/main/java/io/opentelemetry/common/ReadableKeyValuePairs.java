/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.common;

/**
 * A read-only container for String-keyed attributes.
 *
 * @param <V> The type of the values contained in this.
 */
public interface ReadableKeyValuePairs<K, V> {
  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(KeyValueConsumer<K, V> consumer);

  /**
   * Used for iterating over the key-value pairs in a key-value pair container, such as {@link
   * Attributes} or {@link Labels}. The key is always a {@link String}.
   *
   * @param <V> The type of the values contained in the key-value pairs.
   */
  interface KeyValueConsumer<K, V> {
    void consume(K key, V value);
  }
}
