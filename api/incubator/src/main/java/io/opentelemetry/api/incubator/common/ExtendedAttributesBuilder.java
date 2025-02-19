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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/** A builder of {@link ExtendedAttributes} supporting an arbitrary number of key-value pairs. */
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

  /** Puts a {@link ExtendedAttributeKey} with associated value into this. */
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
   */
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
