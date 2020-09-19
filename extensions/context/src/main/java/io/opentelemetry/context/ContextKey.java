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
package io.opentelemetry.context;

/** Key for indexing values of type {@link T} stored in a {@link DefaultContext}. */
public final class ContextKey<T> {

  /**
   * Returns a new {@link ContextKey} with the given debug name. The name does not impact behavior
   * and is only for debugging purposes. Multiple different keys with the same name will be separate
   * keys.
   */
  public static <T> ContextKey<T> named(String name) {
    return new ContextKey<>(name);
  }

  private final String name;

  private ContextKey(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
