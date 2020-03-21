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
import javax.annotation.concurrent.Immutable;

/** Defines the behavior for a span attribute with boolean values. */
@Immutable
public class BooleanAttributeSetter {

  private final String attributeKey;

  /**
   * Constructs an attribute object.
   *
   * @param attributeKey the attribute name/key
   */
  public BooleanAttributeSetter(String attributeKey) {
    super();
    this.attributeKey = attributeKey;
  }

  /**
   * Returns the attribute name.
   *
   * @return the attribute map key
   */
  public String key() {
    return attributeKey;
  }

  /**
   * Sets the attribute on the provided span.
   *
   * @param span the span to add the attribute to
   * @param value the value for this attribute
   */
  public void set(Span span, boolean value) {
    span.setAttribute(key(), value);
  }

  /**
   * Sets the attribute on the provided span if provided a parsable boolean else does nothing.
   *
   * @param span the span to add the attribute to
   * @param value the value for this attribute
   */
  public void trySetParsed(Span span, String value) {
    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      span.setAttribute(key(), Boolean.parseBoolean(value));
    }
  }

  /**
   * Sets the attribute on the provided span to either a boolean if provided string is parsable or
   * else the raw string.
   *
   * @param span the span to add the attribute to
   * @param value the value for this attribute
   */
  public void setParsedOrRaw(Span span, String value) {
    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      span.setAttribute(key(), Boolean.parseBoolean(value));
    } else {
      span.setAttribute(key(), value);
    }
  }
}
