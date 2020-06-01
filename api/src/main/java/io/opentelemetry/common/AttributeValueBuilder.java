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

package io.opentelemetry.common;

/**
 * Helper static methods for creating {@link Attributes} holding {@link AttributeValue} instances.
 */
public class AttributeValueBuilder extends Attributes.Builder<AttributeValue> {

  public static AttributeValueBuilder newBuilder() {
    return new AttributeValueBuilder();
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, String value) {
    return addAttribute(key, AttributeValue.stringAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, long value) {
    return addAttribute(key, AttributeValue.longAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, double value) {
    return addAttribute(key, AttributeValue.doubleAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, boolean value) {
    return addAttribute(key, AttributeValue.booleanAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, String... value) {
    return addAttribute(key, AttributeValue.arrayAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, Long... value) {
    return addAttribute(key, AttributeValue.arrayAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, Double... value) {
    return addAttribute(key, AttributeValue.arrayAttributeValue(value));
  }

  /** javadoc me. */
  public Attributes.Builder<AttributeValue> addAttribute(String key, Boolean... value) {
    return addAttribute(key, AttributeValue.arrayAttributeValue(value));
  }
}
