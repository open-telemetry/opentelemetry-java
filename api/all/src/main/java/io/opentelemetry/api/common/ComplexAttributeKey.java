/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import io.opentelemetry.api.internal.InternalComplexAttributeKeyImpl;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * This interface provides a handle for setting the values of {@link Attributes}. The type of value
 * that can be set with an implementation of this key is denoted by the type parameter.
 *
 * <p>Implementations MUST be immutable, as these are used as the keys to Maps.
 *
 * @param <T> The type of value that can be set with the key.
 */
@Immutable
public interface ComplexAttributeKey<T> {
  /** Returns the underlying String representation of the key. */
  String getKey();

  /** Returns the type of attribute for this key. Useful for building switch statements. */
  ComplexAttributeType getType();

  /** Returns a new AttributeKey for String valued attributes. */
  static ComplexAttributeKey<String> stringKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.STRING);
  }

  /** Returns a new AttributeKey for Boolean valued attributes. */
  static ComplexAttributeKey<Boolean> booleanKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.BOOLEAN);
  }

  /** Returns a new AttributeKey for Long valued attributes. */
  static ComplexAttributeKey<Long> longKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.LONG);
  }

  /** Returns a new AttributeKey for Double valued attributes. */
  static ComplexAttributeKey<Double> doubleKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.DOUBLE);
  }

  /** Returns a new AttributeKey for List&lt;String&gt; valued attributes. */
  static ComplexAttributeKey<List<String>> stringArrayKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.STRING_ARRAY);
  }

  /** Returns a new AttributeKey for List&lt;Boolean&gt; valued attributes. */
  static ComplexAttributeKey<List<Boolean>> booleanArrayKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.BOOLEAN_ARRAY);
  }

  /** Returns a new AttributeKey for List&lt;Long&gt; valued attributes. */
  static ComplexAttributeKey<List<Long>> longArrayKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.LONG_ARRAY);
  }

  /** Returns a new AttributeKey for List&lt;Double&gt; valued attributes. */
  static ComplexAttributeKey<List<Double>> doubleArrayKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.DOUBLE_ARRAY);
  }

  /** Returns a new AttributeKey for Long valued attributes. */
  static ComplexAttributeKey<ComplexAttribute> complexKey(String key) {
    return InternalComplexAttributeKeyImpl.create(key, ComplexAttributeType.COMPLEX);
  }
}
