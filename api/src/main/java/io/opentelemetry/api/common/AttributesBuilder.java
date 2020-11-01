/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A builder of {@link Attributes} supporting an arbitrary number of key-value pairs. */
public class AttributesBuilder {
  private final List<Object> data;

  AttributesBuilder() {
    data = new ArrayList<>();
  }

  AttributesBuilder(List<Object> data) {
    this.data = data;
  }

  /** Create the {@link Attributes} from this. */
  public Attributes build() {
    return Attributes.sortAndFilterToAttributes(data.toArray());
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
    return put(key, (long) value);
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public <T> AttributesBuilder put(AttributeKey<T> key, T value) {
    if (key == null || key.getKey() == null || key.getKey().length() == 0 || value == null) {
      return this;
    }
    data.add(key);
    data.add(value);
    return this;
  }

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public AttributesBuilder put(String key, String value) {
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
  public AttributesBuilder put(String key, long value) {
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
  public AttributesBuilder put(String key, double value) {
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
  public AttributesBuilder put(String key, boolean value) {
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
  public AttributesBuilder put(String key, String... value) {
    return put(stringArrayKey(key), value == null ? null : Arrays.asList(value));
  }

  /**
   * Puts a Long array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public AttributesBuilder put(String key, Long... value) {
    return put(longArrayKey(key), value == null ? null : Arrays.asList(value));
  }

  /**
   * Puts a Double array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public AttributesBuilder put(String key, Double... value) {
    return put(doubleArrayKey(key), value == null ? null : Arrays.asList(value));
  }

  /**
   * Puts a Boolean array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public AttributesBuilder put(String key, Boolean... value) {
    return put(booleanArrayKey(key), value == null ? null : Arrays.asList(value));
  }

  /**
   * Puts all the provided attributes into this Builder.
   *
   * @return this Builder
   */
  public AttributesBuilder putAll(Attributes attributes) {
    data.addAll(attributes.data());
    return this;
  }
}
