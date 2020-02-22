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

import javax.annotation.concurrent.Immutable;

/**
 * Abstract base class for {@link Attribute} implementations.
 *
 * @param <T> the attribute value type
 */
@Immutable
public abstract class AbstractAttribute<T> implements Attribute<T> {

  protected String attributeKey;

  /**
   * Constructs an attribute object.
   *
   * @param attributeKey the attribute name/key
   */
  protected AbstractAttribute(String attributeKey) {
    super();
    this.attributeKey = attributeKey;
  }

  @Override
  public String key() {
    return attributeKey;
  }
}
