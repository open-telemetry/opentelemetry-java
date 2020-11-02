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

  /**
   * Returns the default {@link ContextStorage} used to attach {@link Context}s to scopes of
   * execution. Should only be used when defining your own {@link ContextStorage} in case you want
   * to delegate functionality to the default implementation.
   */
  static ContextStorage threadLocalStorage() {
    return ThreadLocalContextStorage.INSTANCE;
  }

  // Used by auto-instrumentation agent. Check with auto-instrumentation before making changes to
  // this method.
  //
  // In particular, do not change this return type to DefaultContext because auto-instrumentation
  // hijacks this method and returns a bridged implementation of the Context.
  //
  // Ideally auto-instrumentation would hijack Context.root() instead of DefaultContext.root(),
  // but it also need to inject its own implementation of the Context into the class loader at the
  // same time, which causes a problem because injecting a class into the class loader automatically
  // resolves its super classes (interfaces), which in this case is Context, which is the same class
  // (interface) being instrumented at that time, which leads to the JVM throwing a LinkageError
  // "attempted duplicate interface definition"
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
