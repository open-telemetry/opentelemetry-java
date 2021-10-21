/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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
    if (data.size() == 2) {
      return new ArrayBackedAttributes(data.toArray());
    }
    return ArrayBackedAttributes.sortAndFilterToAttributes(data.toArray());
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
    return put(key, (long) value);
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    data.add(key);
    data.add(value);
    return this;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public AttributesBuilder putAll(Attributes attributes) {
    if (attributes == null) {
      return this;
    }
    // Attributes must iterate over their entries with matching types for key / value, so this
    // downcast to the raw type is safe.
    attributes.forEach((key, value) -> put((AttributeKey) key, value));
    return this;
  }

  @Override
  public <T> AttributesBuilder remove(AttributeKey<T> key) {
    if (key == null || key.getKey().isEmpty()) {
      return this;
    }
    return removeIf(
        entryKey ->
            key.getKey().equals(entryKey.getKey()) && key.getType().equals(entryKey.getType()));
  }

  @Override
  public AttributesBuilder remove(String key) {
    if (key == null || key.isEmpty()) {
      return this;
    }
    return removeIf(entryKey -> key.equals(entryKey.getKey()));
  }

  private AttributesBuilder removeIf(Predicate<AttributeKey<?>> predicate) {
    for (int i = 0; i < data.size() - 1; i += 2) {
      Object entry = data.get(i);
      if (entry instanceof AttributeKey && predicate.test((AttributeKey<?>) entry)) {
        // null items are filtered out in ArrayBackedAttributes
        data.set(i, null);
        data.set(i + 1, null);
      }
    }
    return this;
  }

  static List<Double> toList(double... values) {
    Double[] boxed = new Double[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  static List<Long> toList(long... values) {
    Long[] boxed = new Long[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  static List<Boolean> toList(boolean... values) {
    Boolean[] boxed = new Boolean[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }
}
