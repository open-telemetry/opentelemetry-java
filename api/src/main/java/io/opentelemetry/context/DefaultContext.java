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

import javax.annotation.Nullable;

// TODO (trask) replace naive implementation
final class DefaultContext implements Context {

  static final Context EMPTY = new EmptyContext();

  private final Context parent;
  private final Key<?> key;
  private final Object value;

  DefaultContext(Key<?> key, Object value, Context parent) {
    this.key = key;
    this.value = value;
    this.parent = parent;
  }

  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(Key<T> key) {
    if (key.equals(this.key)) {
      return (T) value;
    } else {
      return parent.get(key);
    }
  }

  @Override
  public <T> Context put(Key<T> key, T value) {
    return new DefaultContext(key, value, this);
  }

  static class EmptyContext implements Context {

    @Override
    @Nullable
    public <T> T get(Key<T> key) {
      return null;
    }

    @Override
    public <T> Context put(Key<T> key, T value) {
      return new DefaultContext(key, value, this);
    }
  }
}
