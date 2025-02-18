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

/** A builder of {@link Attributes} supporting an arbitrary number of key-value pairs. */
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

  /** TODO. */
  <T> ExtendedAttributesBuilder put(ExtendedAttributeKey<T> key, T value);

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, String value) {
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
  default ExtendedAttributesBuilder put(String key, long value) {
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
  default ExtendedAttributesBuilder put(String key, double value) {
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
  default ExtendedAttributesBuilder put(String key, boolean value) {
    return put(booleanKey(key), value);
  }

  /** TODO. */
  default <T> ExtendedAttributesBuilder put(String key, ExtendedAttributes value) {
    return put(ExtendedAttributeKey.mapKey(key), value);
  }

  /**
   * Puts a String array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
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
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
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
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
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
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default ExtendedAttributesBuilder put(String key, boolean... value) {
    if (value == null) {
      return this;
    }
    return put(booleanArrayKey(key), toList(value));
  }

  /** TODO. */
  default <T> ExtendedAttributesBuilder put(String key, ExtendedAttributes... value) {
    if (value == null) {
      return this;
    }
    return put(ExtendedAttributeKey.mapArrayKey(key), Arrays.asList(value));
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

  /** TODO. */
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

  /** TODO. */
  default <T> ExtendedAttributesBuilder remove(ExtendedAttributeKey<T> key) {
    if (key == null || key.getKey().isEmpty()) {
      return this;
    }
    // TODO:
    return removeIf(
        entryKey ->
            key.getKey().equals(entryKey.getKey()) && key.getType().equals(entryKey.getType()));
  }

  /** TODO. */
  ExtendedAttributesBuilder removeIf(Predicate<ExtendedAttributeKey<?>> filter);
}
