/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

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
    return ArrayBackedAttributes.sortAndFilterToAttributes(data.toArray());
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
  @SuppressWarnings({"unchecked", "rawtypes"})
  public AttributesBuilder putAll(Attributes attributes) {
    // Attributes must iterate over their entries with matching types for key / value, so this
    // downcast to the raw type is safe.
    attributes.forEach((key, value) -> put((AttributeKey) key, value));
    return this;
  }

  @Nullable
  static List<Double> toList(@Nullable double... values) {
    if (values == null) {
      return null;
    }
    Double[] boxed = new Double[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  @Nullable
  static List<Long> toList(@Nullable long... values) {
    if (values == null) {
      return Collections.emptyList();
    }
    Long[] boxed = new Long[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  @Nullable
  static List<Boolean> toList(@Nullable boolean... values) {
    if (values == null) {
      return null;
    }
    Boolean[] boxed = new Boolean[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }
}
