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
import javax.annotation.concurrent.Immutable;

/** Defines the behavior for a span attribute with long values. */
@Immutable
public class LongAttribute extends AbstractAttribute<Long> {

  /**
   * Constructs an attribute object.
   *
   * @param attributeKey the attribute name/key
   */
  public LongAttribute(String attributeKey) {
    super(attributeKey);
  }

  @Override
  public void set(Span span, @Nullable Long value) {
    if (value != null) {
      span.setAttribute(key(), value);
    } else {
      span.setAttribute(key(), (String) null);
    }
  }
}
