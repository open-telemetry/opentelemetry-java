/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

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
  AttributesBuilder put(String key, String value);

  /**
   * Puts a long attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, long value);

  /**
   * Puts a double attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, double value);

  /**
   * Puts a boolean attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, boolean value);

  /**
   * Puts a String array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, String... value);

  /**
   * Puts a Long array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, long... value);

  /**
   * Puts a Double array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, double... value);

  /**
   * Puts a Boolean array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  AttributesBuilder put(String key, boolean... value);

  /**
   * Puts all the provided attributes into this Builder.
   *
   * @return this Builder
   */
  AttributesBuilder putAll(Attributes attributes);
}
