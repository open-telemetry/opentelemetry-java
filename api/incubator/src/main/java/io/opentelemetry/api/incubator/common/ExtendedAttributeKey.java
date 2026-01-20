/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.internal.InternalExtendedAttributeKeyImpl;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This interface provides a handle for setting the values of {@link ExtendedAttributes}. The type
 * of value that can be set with an implementation of this key is denoted by the type parameter.
 *
 * <p>Implementations MUST be immutable, as these are used as the keys to Maps.
 *
 * <p>The allowed {@link #getType()}s is a superset of those allowed in {@link AttributeKey}.
 *
 * <p>Convenience methods are provided for translating to / from {@link AttributeKey}:
 *
 * <ul>
 *   <li>{@link #asAttributeKey()} converts from {@link ExtendedAttributeKey} to {@link
 *       AttributeKey}
 *   <li>{@link #fromAttributeKey(AttributeKey)} converts from {@link AttributeKey} to {@link
 *       ExtendedAttributeKey}
 * </ul>
 *
 * @param <T> The type of value that can be set with the key.
 * @deprecated Use {@link io.opentelemetry.api.common.AttributeKey} instead. Complex attributes are
 *     now supported directly in the standard API via {@link
 *     io.opentelemetry.api.common.AttributeKey#valueKey(String)}.
 */
@Deprecated
@Immutable
public interface ExtendedAttributeKey<T> {
  /** Returns the underlying String representation of the key. */
  String getKey();

  /** Returns the type of attribute for this key. Useful for building switch statements. */
  ExtendedAttributeType getType();

  /**
   * Return the equivalent {@link AttributeKey}, or {@code null} if the {@link #getType()} has no
   * equivalent {@link io.opentelemetry.api.common.AttributeType}.
   */
  @Nullable
  default AttributeKey<T> asAttributeKey() {
    return InternalExtendedAttributeKeyImpl.toAttributeKey(this);
  }

  /** Return an ExtendedAttributeKey equivalent to the {@code attributeKey}. */
  // TODO (jack-berg): remove once AttributeKey.asExtendedAttributeKey is available
  static <T> ExtendedAttributeKey<T> fromAttributeKey(AttributeKey<T> attributeKey) {
    return InternalExtendedAttributeKeyImpl.toExtendedAttributeKey(attributeKey);
  }

  /** Returns a new ExtendedAttributeKey for String valued attributes. */
  static ExtendedAttributeKey<String> stringKey(String key) {
    return fromAttributeKey(AttributeKey.stringKey(key));
  }

  /** Returns a new ExtendedAttributeKey for Boolean valued attributes. */
  static ExtendedAttributeKey<Boolean> booleanKey(String key) {
    return fromAttributeKey(AttributeKey.booleanKey(key));
  }

  /** Returns a new ExtendedAttributeKey for Long valued attributes. */
  static ExtendedAttributeKey<Long> longKey(String key) {
    return fromAttributeKey(AttributeKey.longKey(key));
  }

  /** Returns a new ExtendedAttributeKey for Double valued attributes. */
  static ExtendedAttributeKey<Double> doubleKey(String key) {
    return fromAttributeKey(AttributeKey.doubleKey(key));
  }

  /** Returns a new ExtendedAttributeKey for List&lt;String&gt; valued attributes. */
  static ExtendedAttributeKey<List<String>> stringArrayKey(String key) {
    return fromAttributeKey(AttributeKey.stringArrayKey(key));
  }

  /** Returns a new ExtendedAttributeKey for List&lt;Boolean&gt; valued attributes. */
  static ExtendedAttributeKey<List<Boolean>> booleanArrayKey(String key) {
    return fromAttributeKey(AttributeKey.booleanArrayKey(key));
  }

  /** Returns a new ExtendedAttributeKey for List&lt;Long&gt; valued attributes. */
  static ExtendedAttributeKey<List<Long>> longArrayKey(String key) {
    return fromAttributeKey(AttributeKey.longArrayKey(key));
  }

  /** Returns a new ExtendedAttributeKey for List&lt;Double&gt; valued attributes. */
  static ExtendedAttributeKey<List<Double>> doubleArrayKey(String key) {
    return fromAttributeKey(AttributeKey.doubleArrayKey(key));
  }

  /**
   * Returns a new ExtendedAttributeKey for {@link ExtendedAttributes} valued attributes.
   *
   * @deprecated Use {@link #valueKey(String)} in combination with {@link Value#of(java.util.Map)}
   *     instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  static ExtendedAttributeKey<ExtendedAttributes> extendedAttributesKey(String key) {
    return InternalExtendedAttributeKeyImpl.create(key, ExtendedAttributeType.EXTENDED_ATTRIBUTES);
  }

  /**
   * Returns a new ExtendedAttributeKey for {@link Value} valued attributes.
   *
   * <p>Simple attributes ({@link ExtendedAttributeType#STRING}, {@link ExtendedAttributeType#LONG},
   * {@link ExtendedAttributeType#DOUBLE}, {@link ExtendedAttributeType#BOOLEAN}, {@link
   * ExtendedAttributeType#STRING_ARRAY}, {@link ExtendedAttributeType#LONG_ARRAY}, {@link
   * ExtendedAttributeType#DOUBLE_ARRAY}, {@link ExtendedAttributeType#BOOLEAN_ARRAY}) SHOULD be
   * used whenever possible. Instrumentations SHOULD assume that backends do not index individual
   * properties of complex attributes, that querying or aggregating on such properties is
   * inefficient and complicated, and that reporting complex attributes carries higher performance
   * overhead.
   */
  static ExtendedAttributeKey<Value<?>> valueKey(String key) {
    return InternalExtendedAttributeKeyImpl.create(key, ExtendedAttributeType.VALUE);
  }
}
