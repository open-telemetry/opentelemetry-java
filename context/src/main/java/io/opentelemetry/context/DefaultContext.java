/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
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

import javax.annotation.Nullable;

final class DefaultContext implements Context {

  private static final Context ROOT = new DefaultContext();

  // Used by auto-instrumentation agent. Check with auto-instrumentation before making changes to
  // this method.
  static Context root() {
    return ROOT;
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
  public <V> V get(ContextKey<V> key) {
    // Because withValue enforces the value for a key is its type, this is always safe.
    @SuppressWarnings("unchecked")
    V value = (V) PersistentHashArrayMappedTrie.get(entries, key);
    return value;
  }

  @Override
  public <V> Context with(ContextKey<V> k1, V v1) {
    PersistentHashArrayMappedTrie.Node<ContextKey<?>, Object> newEntries =
        PersistentHashArrayMappedTrie.put(entries, k1, v1);
    return new DefaultContext(newEntries);
  }
}
