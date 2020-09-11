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

import java.util.List;

/**
 * Creation methods for {@link AttributeKey} implementations.
 *
 * @see Attributes
 */
public class AttributesKeys {
  private AttributesKeys() {}

  /** Create a new AttributeKey for String valued attributes. */
  public static AttributeKey<String> stringKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.STRING);
  }

  /** Create a new AttributeKey for Boolean valued attributes. */
  public static AttributeKey<Boolean> booleanKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.BOOLEAN);
  }

  /** Create a new AttributeKey for Long valued attributes. */
  public static AttributeKey<Long> longKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.LONG);
  }

  /** Create a new AttributeKey for Double valued attributes. */
  public static AttributeKey<Double> doubleKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.DOUBLE);
  }

  /** Create a new AttributeKey for List&lt;String> valued attributes. */
  public static AttributeKey<List<String>> stringArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.STRING_ARRAY);
  }

  /** Create a new AttributeKey for List&lt;Boolean> valued attributes. */
  public static AttributeKey<List<Boolean>> booleanArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.BOOLEAN_ARRAY);
  }

  /** Create a new AttributeKey for List&lt;Long> valued attributes. */
  public static AttributeKey<List<Long>> longArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.LONG_ARRAY);
  }

  /** Create a new AttributeKey for List&lt;Double> valued attributes. */
  public static AttributeKey<List<Double>> doubleArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.DOUBLE_ARRAY);
  }
}
