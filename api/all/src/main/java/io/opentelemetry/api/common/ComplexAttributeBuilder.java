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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/** A builder of {@link ComplexAttribute} supporting an arbitrary number of key-value pairs. */
public interface ComplexAttributeBuilder {
  /** Create the {@link ComplexAttribute} from this. */
  ComplexAttribute build();

  /**
   * Puts a {@link AttributeKey} with associated value into this.
   *
   * <p>The type parameter is unused.
   */
  default ComplexAttributeBuilder put(AttributeKey<Long> key, int value) {
    return put(key, (long) value);
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  <T> ComplexAttributeBuilder put(AttributeKey<T> key, T value);

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default ComplexAttributeBuilder put(String key, String value) {
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
  default ComplexAttributeBuilder put(String key, long value) {
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
  default ComplexAttributeBuilder put(String key, double value) {
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
  default ComplexAttributeBuilder put(String key, boolean value) {
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
  default ComplexAttributeBuilder put(String key, String... value) {
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
  default <T> ComplexAttributeBuilder put(AttributeKey<List<T>> key, T... value) {
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
  default ComplexAttributeBuilder put(String key, long... value) {
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
  default ComplexAttributeBuilder put(String key, double... value) {
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
  default ComplexAttributeBuilder put(String key, boolean... value) {
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
  ComplexAttributeBuilder putAll(Attributes attributes);

  /**
   * Remove all attributes where {@link AttributeKey#getKey()} and {@link AttributeKey#getType()}
   * match the {@code key}.
   *
   * @return this Builder
   */
  <T> ComplexAttributeBuilder remove(AttributeKey<T> key);

  /**
   * Remove all attributes that satisfy the given predicate. Errors or runtime exceptions thrown by
   * the predicate are relayed to the caller.
   *
   * @return this Builder
   */
  ComplexAttributeBuilder removeIf(Predicate<AttributeKey<?>> filter);
}
