/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static io.opentelemetry.api.incubator.common.ArrayBackedExtendedAttributesBuilder.toList;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.booleanArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.booleanKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.doubleArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.doubleKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.longArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.longKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.stringArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * A builder of {@link ExtendedAttributes} supporting an arbitrary number of key-value pairs.
 *
 * @deprecated Use {@link io.opentelemetry.api.common.AttributesBuilder} instead. Complex attributes
 *     are now supported directly in the standard API via {@link
 *     io.opentelemetry.api.common.AttributeKey#valueKey(String)} and {@link
 *     io.opentelemetry.api.common.AttributesBuilder#put(io.opentelemetry.api.common.AttributeKey,
 *     Object)}.
 */
@Deprecated
public interface ExtendedAttributesBuilder {
  /** Create the {@link ExtendedAttributes} from this. */
  ExtendedAttributes build();

  /** Puts a {@link AttributeKey} with associated value into this. */
  default <T> ExtendedAttributesBuilder put(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    return put(ExtendedAttributeKey.fromAttributeKey(key), value);
  }

  /**
   * Puts an {@link ExtendedAttributeKey} with an associated value into this if the value is
   * non-null. Providing a null value does not remove or unset previously set values.
   *
   * <p>Simple attributes ({@link ExtendedAttributeType#STRING}, {@link ExtendedAttributeType#LONG},
   * {@link ExtendedAttributeType#DOUBLE}, {@link ExtendedAttributeType#BOOLEAN}, {@link
   * ExtendedAttributeType#STRING_ARRAY}, {@link ExtendedAttributeType#LONG_ARRAY}, {@link
   * ExtendedAttributeType#DOUBLE_ARRAY}, {@link ExtendedAttributeType#BOOLEAN_ARRAY}) SHOULD be
   * used whenever possible. Instrumentations SHOULD assume that backends do not index individual
   * properties of complex attributes, that querying or aggregating on such properties is
   * inefficient and complicated, and that reporting complex attributes carries higher performance
   * overhead.
   *
   * <p>Note: This method will automatically convert complex attributes ({@link
   * ExtendedAttributeType#VALUE}) to simple attributes when possible.
   *
   * <ul>
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of("a"))} is equivalent to
   *       calling {@code put(ExtendedAttributeKey.stringKey("key"), "a")}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(1L))} is equivalent to
   *       calling {@code put(ExtendedAttributeKey.longKey("key"), 1L)}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(1.0))} is equivalent to
   *       calling {@code put(ExtendedAttributeKey.doubleKey("key"), 1.0)}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(true))} is equivalent
   *       to calling {@code put(ExtendedAttributeKey.booleanKey("key"), true)}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(Value.of("a"),
   *       Value.of("b")))} is equivalent to calling {@code
   *       put(ExtendedAttributeKey.stringArrayKey("key"), Arrays.asList("a", "b"))}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(Value.of(1L),
   *       Value.of(2L)))} is equivalent to calling {@code
   *       put(ExtendedAttributeKey.longArrayKey("key"), Arrays.asList(1L, 2L))}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(Value.of(1.0),
   *       Value.of(2.0)))} is equivalent to calling {@code
   *       put(ExtendedAttributeKey.doubleArrayKey("key"), Arrays.asList(1.0, 2.0))}.
   *   <li>Calling {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(Value.of(true),
   *       Value.of(false)))} is equivalent to calling {@code
   *       put(ExtendedAttributeKey.booleanArrayKey("key"), Arrays.asList(true, false))}.
   * </ul>
   */
  <T> ExtendedAttributesBuilder put(ExtendedAttributeKey<T> key, T value);

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, String value) {
    return put(stringKey(key), value);
  }

  /**
   * Puts a long attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, long value) {
    return put(longKey(key), value);
  }

  /**
   * Puts a double attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, double value) {
    return put(doubleKey(key), value);
  }

  /**
   * Puts a boolean attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, boolean value) {
    return put(booleanKey(key), value);
  }

  /**
   * Puts a {@link ExtendedAttributes} attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   * @deprecated Use {@link #put(ExtendedAttributeKey, Object)} with {@link Value#of(java.util.Map)}
   *     instead.
   */
  @Deprecated
  default <T> ExtendedAttributesBuilder put(String key, ExtendedAttributes value) {
    return put(ExtendedAttributeKey.extendedAttributesKey(key), value);
  }

  /**
   * Puts a String array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, String... value) {
    if (value == null) {
      return this;
    }
    return put(stringArrayKey(key), Arrays.asList(value));
  }

  /**
   * Puts a List attribute into this.
   *
   * @return this Builder
   */
  @SuppressWarnings("unchecked")
  default <T> ExtendedAttributesBuilder put(AttributeKey<List<T>> key, T... value) {
    if (value == null) {
      return this;
    }
    return put(key, Arrays.asList(value));
  }

  /**
   * Puts a Long array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, long... value) {
    if (value == null) {
      return this;
    }
    return put(longArrayKey(key), toList(value));
  }

  /**
   * Puts a Double array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, double... value) {
    if (value == null) {
      return this;
    }
    return put(doubleArrayKey(key), toList(value));
  }

  /**
   * Puts a Boolean array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(ExtendedAttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, boolean... value) {
    if (value == null) {
      return this;
    }
    return put(booleanArrayKey(key), toList(value));
  }

  /**
   * Puts all the provided attributes into this Builder.
   *
   * @return this Builder
   */
  @SuppressWarnings({"unchecked"})
  default ExtendedAttributesBuilder putAll(Attributes attributes) {
    if (attributes == null) {
      return this;
    }
    attributes.forEach((key, value) -> put((AttributeKey<Object>) key, value));
    return this;
  }

  /**
   * Puts all the provided attributes into this Builder.
   *
   * @return this Builder
   */
  @SuppressWarnings({"unchecked"})
  default ExtendedAttributesBuilder putAll(ExtendedAttributes attributes) {
    if (attributes == null) {
      return this;
    }
    attributes.forEach((key, value) -> put((ExtendedAttributeKey<Object>) key, value));
    return this;
  }

  /**
   * Remove all attributes where {@link AttributeKey#getKey()} and {@link AttributeKey#getType()}
   * match the {@code key}.
   *
   * @return this Builder
   */
  default <T> ExtendedAttributesBuilder remove(AttributeKey<T> key) {
    return remove(ExtendedAttributeKey.fromAttributeKey(key));
  }

  /**
   * Remove all attributes where {@link ExtendedAttributeKey#getKey()} and {@link
   * ExtendedAttributeKey#getType()} match the {@code key}.
   *
   * @return this Builder
   */
  default <T> ExtendedAttributesBuilder remove(ExtendedAttributeKey<T> key) {
    if (key == null || key.getKey().isEmpty()) {
      return this;
    }
    return removeIf(
        entryKey ->
            key.getKey().equals(entryKey.getKey()) && key.getType().equals(entryKey.getType()));
  }

  /**
   * Remove all attributes that satisfy the given predicate. Errors or runtime exceptions thrown by
   * the predicate are relayed to the caller.
   *
   * @return this Builder
   */
  ExtendedAttributesBuilder removeIf(Predicate<ExtendedAttributeKey<?>> filter);
}
