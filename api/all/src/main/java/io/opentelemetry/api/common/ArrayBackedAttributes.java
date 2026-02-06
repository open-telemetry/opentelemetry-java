/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ArrayBackedAttributes extends ImmutableKeyValuePairs<AttributeKey<?>, Object>
    implements Attributes {

  // We only compare the key name, not type, when constructing, to allow deduping keys with the
  // same name but different type.
  private static final Comparator<AttributeKey<?>> KEY_COMPARATOR_FOR_CONSTRUCTION =
      Comparator.comparing(AttributeKey::getKey);

  static final Attributes EMPTY = Attributes.builder().build();

  private ArrayBackedAttributes(Object[] data, Comparator<AttributeKey<?>> keyComparator) {
    super(data, keyComparator);
  }

  /**
   * Only use this constructor if you can guarantee that the data has been de-duped, sorted by key
   * and contains no null values or null/empty keys.
   *
   * @param data the raw data
   */
  ArrayBackedAttributes(Object[] data) {
    super(data);
  }

  @Override
  public AttributesBuilder toBuilder() {
    return new ArrayBackedAttributesBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked") // safe cast: values are stored internally keyed by AttributeKey<T>
  @Override
  @Nullable
  public <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    if (key.getType() == AttributeType.VALUE) {
      return (T) getAsValue(key.getKey());
    }
    // Check if we're looking for an array type but have a VALUE with empty array
    if (isArrayType(key.getType())) {
      T value = (T) super.get(key);
      if (value == null) {
        // Check if there's a VALUE with the same key that contains an empty array
        Value<?> valueAttr = getValueAttribute(key.getKey());
        if (valueAttr != null && isEmptyArray(valueAttr)) {
          return (T) Collections.emptyList();
        }
      }
      return value;
    }
    return (T) super.get(key);
  }

  private static boolean isArrayType(AttributeType type) {
    return type == AttributeType.STRING_ARRAY
        || type == AttributeType.LONG_ARRAY
        || type == AttributeType.DOUBLE_ARRAY
        || type == AttributeType.BOOLEAN_ARRAY;
  }

  @Nullable
  private Value<?> getValueAttribute(String keyName) {
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      AttributeKey<?> currentKey = (AttributeKey<?>) data.get(i);
      if (currentKey.getKey().equals(keyName) && currentKey.getType() == AttributeType.VALUE) {
        return (Value<?>) data.get(i + 1);
      }
    }
    return null;
  }

  private static boolean isEmptyArray(Value<?> value) {
    if (value.getType() != ValueType.ARRAY) {
      return false;
    }
    @SuppressWarnings("unchecked")
    List<Value<?>> arrayValues = (List<Value<?>>) value.getValue();
    return arrayValues.isEmpty();
  }

  @Nullable
  private Value<?> getAsValue(String keyName) {
    // Find any attribute with the same key name and convert it to Value
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      AttributeKey<?> currentKey = (AttributeKey<?>) data.get(i);
      if (currentKey.getKey().equals(keyName)) {
        Object value = data.get(i + 1);
        return asValue(currentKey.getType(), value);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  private static Value<?> asValue(AttributeType type, Object value) {
    switch (type) {
      case STRING:
        return Value.of((String) value);
      case LONG:
        return Value.of((Long) value);
      case DOUBLE:
        return Value.of((Double) value);
      case BOOLEAN:
        return Value.of((Boolean) value);
      case STRING_ARRAY:
        List<String> stringList = (List<String>) value;
        Value<?>[] stringValues = new Value<?>[stringList.size()];
        for (int i = 0; i < stringList.size(); i++) {
          stringValues[i] = Value.of(stringList.get(i));
        }
        return Value.of(stringValues);
      case LONG_ARRAY:
        List<Long> longList = (List<Long>) value;
        Value<?>[] longValues = new Value<?>[longList.size()];
        for (int i = 0; i < longList.size(); i++) {
          longValues[i] = Value.of(longList.get(i));
        }
        return Value.of(longValues);
      case DOUBLE_ARRAY:
        List<Double> doubleList = (List<Double>) value;
        Value<?>[] doubleValues = new Value<?>[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
          doubleValues[i] = Value.of(doubleList.get(i));
        }
        return Value.of(doubleValues);
      case BOOLEAN_ARRAY:
        List<Boolean> booleanList = (List<Boolean>) value;
        Value<?>[] booleanValues = new Value<?>[booleanList.size()];
        for (int i = 0; i < booleanList.size(); i++) {
          booleanValues[i] = Value.of(booleanList.get(i));
        }
        return Value.of(booleanValues);
      case VALUE:
        // Already a Value
        return (Value<?>) value;
    }
    // Should not reach here
    return null;
  }

  static Attributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      AttributeKey<?> key = (AttributeKey<?>) data[i];
      if (key != null && key.getKey().isEmpty()) {
        data[i] = null;
      }
    }
    return new ArrayBackedAttributes(data, KEY_COMPARATOR_FOR_CONSTRUCTION);
  }
}
