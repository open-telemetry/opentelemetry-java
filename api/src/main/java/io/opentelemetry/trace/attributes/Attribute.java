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

package io.opentelemetry.trace.attributes;

import io.opentelemetry.trace.Span;
import javax.annotation.Nullable;

/** Defines the behavior for one attribute definition. */
public interface Attribute<T> {

  /**
   * Returns the attribute name.
   *
   * @return the attribute map key
   */
  String key();

  /**
   * Sets the attribute on the provided span.
   *
   * @param span the span to add the attribute to
   * @param value the value for this attribute or <code>null</code> to remove attribute from span
   */
  void set(Span span, @Nullable T value);
}
