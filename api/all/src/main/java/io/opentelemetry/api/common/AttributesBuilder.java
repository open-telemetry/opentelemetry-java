/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.ArrayBackedAttributesBuilder.toList;
import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** A builder of {@link Attributes} supporting an arbitrary number of key-value pairs. */
public interface AttributesBuilder {
  /** Create the {@link Attributes} from this. */
  Attributes build();

  /**
   * Puts a {@link AttributeKey} with associated value into this.
   *
   * <p>The type parameter is unused.
   */
  // The type parameter was added unintentionally and unfortunately it is an API break for
  // implementations of this interface to remove it. It doesn't affect users of the interface in
  // any way, and has almost no effect on implementations, so we leave it until a future major
  // version.
  <T> AttributesBuilder put(AttributeKey<Long> key, int value);

  /**
   * Puts an {@link AttributeKey} with an associated value into this if the value is non-null.
   * Providing a null value does not remove or unset previously set values.
   *
   * <p>Simple attributes ({@link AttributeType#STRING}, {@link AttributeType#LONG}, {@link
   * AttributeType#DOUBLE}, {@link AttributeType#BOOLEAN}, {@link AttributeType#STRING_ARRAY},
   * {@link AttributeType#LONG_ARRAY}, {@link AttributeType#DOUBLE_ARRAY}, {@link
   * AttributeType#BOOLEAN_ARRAY}) SHOULD be used whenever possible. Instrumentations SHOULD assume
   * that backends do not index individual properties of complex attributes, that querying or
   * aggregating on such properties is inefficient and complicated, and that reporting complex
   * attributes carries higher performance overhead.
   *
   * <p>Note: This method will automatically convert complex attributes ({@link
   * AttributeType#VALUE}) to simple attributes when possible.
   *
   * <ul>
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of("a"))} is equivalent to calling
   *       {@code put(AttributeKey.stringKey("key"), "a")}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(1L))} is equivalent to calling
   *       {@code put(AttributeKey.longKey("key"), 1L)}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(1.0))} is equivalent to calling
   *       {@code put(AttributeKey.doubleKey("key"), 1.0)}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(true))} is equivalent to
   *       calling {@code put(AttributeKey.booleanKey("key"), true)}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(Value.of("a"), Value.of("b")))}
   *       is equivalent to calling {@code put(AttributeKey.stringArrayKey("key"),
   *       Arrays.asList("a", "b"))}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(Value.of(1L), Value.of(2L)))}
   *       is equivalent to calling {@code put(AttributeKey.longArrayKey("key"), Arrays.asList(1L,
   *       2L))}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(Value.of(1.0), Value.of(2.0)))}
   *       is equivalent to calling {@code put(AttributeKey.doubleArrayKey("key"),
   *       Arrays.asList(1.0, 2.0))}.
   *   <li>Calling {@code put(AttributeKey.valueKey("key"), Value.of(Value.of(true),
   *       Value.of(false)))} is equivalent to calling {@code
   *       put(AttributeKey.booleanArrayKey("key"), Arrays.asList(true, false))}.
   * </ul>
   */
  <T> AttributesBuilder put(AttributeKey<T> key, @Nullable T value);

  /**
   * Puts a String attribute into this if the value is non-null. Providing a null value does not
   * remove or unset previously set values.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, @Nullable String value) {
    return put(stringKey(key), value);
  }

  /**
   * Puts a long attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, long value) {
    return put(longKey(key), value);
  }

  /**
   * Puts a double attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, double value) {
    return put(doubleKey(key), value);
  }

  /**
   * Puts a boolean attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, boolean value) {
    return put(booleanKey(key), value);
  }

  /**
   * Puts a String array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, String... value) {
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
  default <T> AttributesBuilder put(AttributeKey<List<T>> key, T... value) {
    if (value == null) {
      return this;
    }
    return put(key, Arrays.asList(value));
  }

  /**
   * Puts a Long array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, long... value) {
    if (value == null) {
      return this;
    }
    return put(longArrayKey(key), toList(value));
  }

  /**
   * Puts a Double array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, double... value) {
    if (value == null) {
      return this;
    }
    return put(doubleArrayKey(key), toList(value));
  }

  /**
   * Puts a Boolean array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, boolean... value) {
    if (value == null) {
      return this;
    }
    return put(booleanArrayKey(key), toList(value));
  }

  /**
   * Puts a {@link Value} attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, Value<?> value) {
    return put(valueKey(key), value);
  }

  /**
   * Puts all the provided attributes into this Builder.
   *
   * @return this Builder
   */
  AttributesBuilder putAll(Attributes attributes);

  /**
   * Remove all attributes where {@link AttributeKey#getKey()} and {@link AttributeKey#getType()}
   * match the {@code key}.
   *
   * @return this Builder
   */
  default <T> AttributesBuilder remove(AttributeKey<T> key) {
    // default implementation is no-op
    return this;
  }

  /**
   * Remove all attributes that satisfy the given predicate. Errors or runtime exceptions thrown by
   * the predicate are relayed to the caller.
   *
   * @return this Builder
   */
  default AttributesBuilder removeIf(Predicate<AttributeKey<?>> filter) {
    // default implementation is no-op
    return this;
  }
}
