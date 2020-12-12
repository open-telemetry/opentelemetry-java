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

import java.util.Arrays;
import javax.annotation.Nullable;

final class ArrayBasedContext implements Context {

  private static final Context ROOT = new ArrayBasedContext(new Object[0]);

  // Used by auto-instrumentation agent. Check with auto-instrumentation before making changes to
  // this method.
  //
  // In particular, do not change this return type to DefaultContext because auto-instrumentation
  // hijacks this method and returns a bridged implementation of Context.
  //
  // Ideally auto-instrumentation would hijack the public Context.root() instead of this
  // method, but auto-instrumentation also needs to inject its own implementation of Context
  // into the class loader at the same time, which causes a problem because injecting a class into
  // the class loader automatically resolves its super classes (interfaces), which in this case is
  // Context, which would be the same class (interface) being instrumented at that time,
  // which would lead to the JVM throwing a LinkageError "attempted duplicate interface definition"
  static Context root() {
    return ROOT;
  }

  private final Object[] entries;

  private ArrayBasedContext(Object[] entries) {
    this.entries = entries;
  }

  @Override
  @Nullable
  public <V> V get(ContextKey<V> key) {
    for (int i = 0; i < entries.length; i += 2) {
      if (entries[i] == key) {
        @SuppressWarnings("unchecked")
        V result = (V) entries[i + 1];
        return result;
      }
    }
    return null;
  }

  @Override
  public <V> Context with(ContextKey<V> key, V value) {
    for (int i = 0; i < entries.length; i += 2) {
      if (entries[i] == key) {
        if (entries[i + 1] == value) {
          return this;
        }
        Object[] newEntries = entries.clone();
        newEntries[i + 1] = value;
        return new ArrayBasedContext(newEntries);
      }
    }
    Object[] newEntries = Arrays.copyOf(entries, entries.length + 2);
    newEntries[newEntries.length - 2] = key;
    newEntries[newEntries.length - 1] = value;
    return new ArrayBasedContext(newEntries);
  }
}
