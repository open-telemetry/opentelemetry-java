/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import io.opentelemetry.api.internal.InternalExtendedAttributeKeyImpl;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** TODO. */
@Immutable
public interface ExtendedAttributeKey<T> {
  /** Returns the underlying String representation of the key. */
  String getKey();

  /** Returns the type of attribute for this key. Useful for building switch statements. */
  ExtendedAttributeType getType();

  @Nullable
  default AttributeKey<T> asAttributeKey() {
    return InternalExtendedAttributeKeyImpl.toAttributeKey(this);
  }

  /** Returns a new ExtendedAttributeKey for String valued attributes. */
  static ExtendedAttributeKey<String> stringKey(String key) {
    return AttributeKey.stringKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for Boolean valued attributes. */
  static ExtendedAttributeKey<Boolean> booleanKey(String key) {
    return AttributeKey.booleanKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for Long valued attributes. */
  static ExtendedAttributeKey<Long> longKey(String key) {
    return AttributeKey.longKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for Double valued attributes. */
  static ExtendedAttributeKey<Double> doubleKey(String key) {
    return AttributeKey.doubleKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for List&lt;String&gt; valued attributes. */
  static ExtendedAttributeKey<List<String>> stringArrayKey(String key) {
    return AttributeKey.stringArrayKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for List&lt;Boolean&gt; valued attributes. */
  static ExtendedAttributeKey<List<Boolean>> booleanArrayKey(String key) {
    return AttributeKey.booleanArrayKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for List&lt;Long&gt; valued attributes. */
  static ExtendedAttributeKey<List<Long>> longArrayKey(String key) {
    return AttributeKey.longArrayKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for List&lt;Double&gt; valued attributes. */
  static ExtendedAttributeKey<List<Double>> doubleArrayKey(String key) {
    return AttributeKey.doubleArrayKey(key).asExtendedAttributeKey();
  }

  /** Returns a new ExtendedAttributeKey for Map valued attributes. */
  static ExtendedAttributeKey<ExtendedAttributes> mapKey(String key) {
    return InternalExtendedAttributeKeyImpl.create(key, ExtendedAttributeType.MAP);
  }

  /** Returns a new ExtendedAttributeKey for Map array valued attributes. */
  static ExtendedAttributeKey<List<ExtendedAttributes>> mapArrayKey(String key) {
    return InternalExtendedAttributeKeyImpl.create(key, ExtendedAttributeType.MAP_ARRAY);
  }
}
