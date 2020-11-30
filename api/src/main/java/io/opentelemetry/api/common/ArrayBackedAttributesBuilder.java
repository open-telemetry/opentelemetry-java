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

class ArrayBackedAttributesBuilder implements AttributesBuilder {
  private final List<Object> data;

  ArrayBackedAttributesBuilder() {
    data = new ArrayList<>();
  }

  ArrayBackedAttributesBuilder(List<Object> data) {
    this.data = data;
  }

  @Override
  public Attributes build() {
    return Attributes.ArrayBackedAttributes.sortAndFilterToAttributes(data.toArray());
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
    return put(key, (long) value);
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<T> key, T value) {
    if (key == null || key.getKey() == null || key.getKey().length() == 0 || value == null) {
      return this;
    }
    data.add(key);
    data.add(value);
    return this;
  }

  @Override
  public AttributesBuilder put(String key, String value) {
    return put(stringKey(key), value);
  }

  @Override
  public AttributesBuilder put(String key, long value) {
    return put(longKey(key), value);
  }

  @Override
  public AttributesBuilder put(String key, double value) {
    return put(doubleKey(key), value);
  }

  @Override
  public AttributesBuilder put(String key, boolean value) {
    return put(booleanKey(key), value);
  }

  @Override
  public AttributesBuilder put(String key, String... value) {
    return put(stringArrayKey(key), value == null ? null : Arrays.asList(value));
  }

  @Override
  public AttributesBuilder put(String key, long... value) {
    return put(longArrayKey(key), value == null ? null : toList(value));
  }

  @Override
  public AttributesBuilder put(String key, double... value) {
    return put(doubleArrayKey(key), value == null ? null : toList(value));
  }

  @Override
  public AttributesBuilder put(String key, boolean... value) {
    return put(booleanArrayKey(key), value == null ? null : toList(value));
  }

  @Override
  public AttributesBuilder putAll(Attributes attributes) {
    attributes.forEach(this::put);
    return this;
  }

  private static List<Double> toList(double... values) {
    Double[] boxed = new Double[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  private static List<Long> toList(long... values) {
    Long[] boxed = new Long[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  private static List<Boolean> toList(boolean... values) {
    Boolean[] boxed = new Boolean[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }
}
