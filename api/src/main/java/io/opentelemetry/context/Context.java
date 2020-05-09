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

package io.opentelemetry.context;

// TODO (trask) javadoc class and methods
public interface Context {

  Context EMPTY = DefaultContext.EMPTY;

  // returns default value from key
  <T> T get(Key<T> key);

  <T> Context put(Key<T> key, T value);

  class Key<T> {
    private final String name;
    private final T defaultValue;

    public Key(String name) {
      this(name, null);
    }

    public Key(String name, T defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
    }

    public T getDefaultValue() {
      return defaultValue;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
