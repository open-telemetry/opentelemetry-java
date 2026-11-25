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
import java.util.List;

final class AttributeValueConverter {

  @SuppressWarnings("unchecked")
  static AttributeKey<?> toAttributeKey(AttributeKey<?> key, Value<?> value) {
    String keyName = key.getKey();
    switch (value.getType()) {
      case STRING:
        return stringKey(keyName);
      case LONG:
        return longKey(keyName);
      case DOUBLE:
        return doubleKey(keyName);
      case BOOLEAN:
        return booleanKey(keyName);
      case ARRAY:
        List<Value<?>> arrayValues = (List<Value<?>>) value.getValue();
        if (arrayValues.isEmpty()) {
          return key;
        }
        ValueType elementType = arrayValues.get(0).getType();
        for (Value<?> element : arrayValues) {
          if (element.getType() != elementType) {
            return key;
          }
        }
        switch (elementType) {
          case STRING:
            return stringArrayKey(keyName);
          case LONG:
            return longArrayKey(keyName);
          case DOUBLE:
            return doubleArrayKey(keyName);
          case BOOLEAN:
            return booleanArrayKey(keyName);
          case ARRAY:
          case KEY_VALUE_LIST:
          case BYTES:
          case EMPTY:
            return key;
        }
        throw new IllegalArgumentException("Unsupported element type: " + elementType);
      case KEY_VALUE_LIST:
      case BYTES:
      case EMPTY:
        return key;
    }
    throw new IllegalArgumentException("Unsupported value type: " + value.getType());
  }

  // Only call after toAttributeKey has narrowed the key's type.
  @SuppressWarnings("unchecked")
  static Object toAttributeValue(Value<?> value) {
    if (value.getType() != ValueType.ARRAY) {
      return value.getValue();
    }
    List<Value<?>> arrayValues = (List<Value<?>>) value.getValue();
    List<Object> values = new ArrayList<>(arrayValues.size());
    for (Value<?> element : arrayValues) {
      values.add(element.getValue());
    }
    return values;
  }

  private AttributeValueConverter() {}
}
