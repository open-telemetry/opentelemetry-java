/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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

  /** Create a new AttributeKey for List&lt;String&gt; valued attributes. */
  public static AttributeKey<List<String>> stringArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.STRING_ARRAY);
  }

  /** Create a new AttributeKey for List&lt;Boolean&gt; valued attributes. */
  public static AttributeKey<List<Boolean>> booleanArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.BOOLEAN_ARRAY);
  }

  /** Create a new AttributeKey for List&lt;Long&gt; valued attributes. */
  public static AttributeKey<List<Long>> longArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.LONG_ARRAY);
  }

  /** Create a new AttributeKey for List&lt;Double&gt; valued attributes. */
  public static AttributeKey<List<Double>> doubleArrayKey(String key) {
    return AttributeKeyImpl.create(key, AttributeType.DOUBLE_ARRAY);
  }
}
