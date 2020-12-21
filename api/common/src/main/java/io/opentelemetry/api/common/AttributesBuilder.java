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
import java.util.Collections;

/** A builder of {@link Attributes} supporting an arbitrary number of key-value pairs. */
public interface AttributesBuilder {
  /** Create the {@link Attributes} from this. */
  Attributes build();

  /** Puts a {@link AttributeKey} with associated value into this. */
  <T> AttributesBuilder put(AttributeKey<Long> key, int value);

  /** Puts a {@link AttributeKey} with associated value into this. */
  <T> AttributesBuilder put(AttributeKey<T> key, T value);

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  default AttributesBuilder put(String key, String value) {
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
    return put(stringArrayKey(key), value == null ? Collections.emptyList() : Arrays.asList(value));
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
    return put(booleanArrayKey(key), toList(value));
  }

  /**
   * Puts all the provided attributes into this Builder.
   *
   * @return this Builder
   */
  AttributesBuilder putAll(Attributes attributes);
}
