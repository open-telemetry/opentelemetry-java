/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.booleanArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.booleanKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.doubleArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.doubleKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.longArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.longKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.stringArrayKey;
import static io.opentelemetry.api.incubator.common.ExtendedAttributeKey.stringKey;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

class ArrayBackedExtendedAttributesBuilder implements ExtendedAttributesBuilder {
  private final List<Object> data;

  ArrayBackedExtendedAttributesBuilder() {
    data = new ArrayList<>();
  }

  ArrayBackedExtendedAttributesBuilder(List<Object> data) {
    this.data = data;
  }

  @Override
  public ExtendedAttributes build() {
    // If only one key-value pair AND the entry hasn't been set to null (by #remove(AttributeKey<T>)
    // or #removeIf(Predicate<AttributeKey<?>>)), then we can bypass sorting and filtering
    if (data.size() == 2 && data.get(0) != null) {
      return new ArrayBackedExtendedAttributes(data.toArray());
    }
    return ArrayBackedExtendedAttributes.sortAndFilterToAttributes(data.toArray());
  }

  @Override
  public <T> ExtendedAttributesBuilder put(ExtendedAttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    if (key.getType() == ExtendedAttributeType.VALUE && value instanceof Value) {
      putValue(key, (Value<?>) value);
      return this;
    }
    data.add(key);
    data.add(value);
    return this;
  }

  @SuppressWarnings("unchecked")
  private void putValue(ExtendedAttributeKey<?> key, Value<?> valueObj) {
    // Convert VALUE type to narrower type when possible
    String keyName = key.getKey();
    switch (valueObj.getType()) {
      case STRING:
        put(stringKey(keyName), ((Value<String>) valueObj).getValue());
        return;
      case LONG:
        put(longKey(keyName), ((Value<Long>) valueObj).getValue());
        return;
      case DOUBLE:
        put(doubleKey(keyName), ((Value<Double>) valueObj).getValue());
        return;
      case BOOLEAN:
        put(booleanKey(keyName), ((Value<Boolean>) valueObj).getValue());
        return;
      case ARRAY:
        List<Value<?>> arrayValues = (List<Value<?>>) valueObj.getValue();
        ExtendedAttributeType attributeType = attributeType(arrayValues);
        switch (attributeType) {
          case STRING_ARRAY:
            List<String> strings = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              strings.add((String) v.getValue());
            }
            put(stringArrayKey(keyName), strings);
            return;
          case LONG_ARRAY:
            List<Long> longs = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              longs.add((Long) v.getValue());
            }
            put(longArrayKey(keyName), longs);
            return;
          case DOUBLE_ARRAY:
            List<Double> doubles = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              doubles.add((Double) v.getValue());
            }
            put(doubleArrayKey(keyName), doubles);
            return;
          case BOOLEAN_ARRAY:
            List<Boolean> booleans = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              booleans.add((Boolean) v.getValue());
            }
            put(booleanArrayKey(keyName), booleans);
            return;
          case VALUE:
            // Not coercible (empty, non-homogeneous, or unsupported element type)
            data.add(key);
            data.add(valueObj);
            return;
          case EXTENDED_ATTRIBUTES:
            // Not coercible
            data.add(key);
            data.add(valueObj);
            return;
          default:
            throw new IllegalArgumentException("Unexpected array attribute type: " + attributeType);
        }
      case KEY_VALUE_LIST:
      case BYTES:
        // Keep as VALUE type
        data.add(key);
        data.add(valueObj);
        return;
    }
  }

  /**
   * Returns the ExtendedAttributeType for a homogeneous array (STRING_ARRAY, LONG_ARRAY,
   * DOUBLE_ARRAY, or BOOLEAN_ARRAY), or VALUE if the array is empty, non-homogeneous, or contains
   * unsupported element types.
   */
  private static ExtendedAttributeType attributeType(List<Value<?>> arrayValues) {
    if (arrayValues.isEmpty()) {
      return ExtendedAttributeType.VALUE;
    }
    ValueType elementType = arrayValues.get(0).getType();
    for (Value<?> v : arrayValues) {
      if (v.getType() != elementType) {
        return ExtendedAttributeType.VALUE;
      }
    }
    switch (elementType) {
      case STRING:
        return ExtendedAttributeType.STRING_ARRAY;
      case LONG:
        return ExtendedAttributeType.LONG_ARRAY;
      case DOUBLE:
        return ExtendedAttributeType.DOUBLE_ARRAY;
      case BOOLEAN:
        return ExtendedAttributeType.BOOLEAN_ARRAY;
      case ARRAY:
      case KEY_VALUE_LIST:
      case BYTES:
        return ExtendedAttributeType.VALUE;
    }
    throw new IllegalArgumentException("Unsupported element type: " + elementType);
  }

  @Override
  public ExtendedAttributesBuilder removeIf(Predicate<ExtendedAttributeKey<?>> predicate) {
    if (predicate == null) {
      return this;
    }
    for (int i = 0; i < data.size() - 1; i += 2) {
      Object entry = data.get(i);
      if (entry instanceof ExtendedAttributeKey
          && predicate.test((ExtendedAttributeKey<?>) entry)) {
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
