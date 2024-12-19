/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

class ArrayBackedComplexAttributeBuilder implements ComplexAttributeBuilder {
  private final List<Object> data;

  ArrayBackedComplexAttributeBuilder() {
    data = new ArrayList<>();
  }

  ArrayBackedComplexAttributeBuilder(List<Object> data) {
    this.data = data;
  }

  @Override
  public ComplexAttribute build() {
    // If only one key-value pair AND the entry hasn't been set to null (by
    // #remove(ComplexAttributeKey<T>)
    // or #removeIf(Predicate<ComplexAttributeKey<?>>)), then we can bypass sorting and filtering
    if (data.size() == 2 && data.get(0) != null) {
      return new ArrayBackedComplexAttribute(data.toArray());
    }
    return ArrayBackedComplexAttribute.sortAndFilterToAttributes(data.toArray());
  }

  @Override
  public <T> ComplexAttributeBuilder put(ComplexAttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    data.add(key);
    data.add(value);
    return this;
  }

  @Override
  public <T> ComplexAttributeBuilder remove(ComplexAttributeKey<T> key) {
    if (key == null || key.getKey().isEmpty()) {
      return this;
    }
    return removeIf(
        entryKey ->
            key.getKey().equals(entryKey.getKey()) && key.getType().equals(entryKey.getType()));
  }

  @Override
  public ComplexAttributeBuilder removeIf(Predicate<ComplexAttributeKey<?>> predicate) {
    if (predicate == null) {
      return this;
    }
    for (int i = 0; i < data.size() - 1; i += 2) {
      Object entry = data.get(i);
      if (entry instanceof ComplexAttributeKey && predicate.test((ComplexAttributeKey<?>) entry)) {
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
