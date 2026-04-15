/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class AttributeUtil {

  private AttributeUtil() {}

  /**
   * Apply the {@code countLimit} and {@code lengthLimit} to the attributes.
   *
   * <p>If all attributes fall within the limits, return as is. Else, return an attributes instance
   * with the limits applied. {@code countLimit} limits the number of unique attribute keys. {@code
   * lengthLimit} limits the length of attribute string and string list values.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Attributes applyAttributesLimit(
      Attributes attributes, int countLimit, int lengthLimit) {
    if (attributes.isEmpty() || attributes.size() <= countLimit) {
      if (lengthLimit == Integer.MAX_VALUE) {
        return attributes;
      }
      boolean allValidLength =
          allMatch(attributes.asMap().values(), value -> isValidLength(value, lengthLimit));
      if (allValidLength) {
        return attributes;
      }
    }

    AttributesBuilder result = Attributes.builder();
    int i = 0;
    for (Map.Entry<AttributeKey<?>, Object> entry : attributes.asMap().entrySet()) {
      if (i >= countLimit) {
        break;
      }
      result.put(
          (AttributeKey) entry.getKey(), applyAttributeLengthLimit(entry.getValue(), lengthLimit));
      i++;
    }
    return result.build();
  }

  private static boolean isValidLength(Object value, int lengthLimit) {
    if (value instanceof List) {
      return allMatch((List<?>) value, entry -> isValidLength(entry, lengthLimit));
    } else if (value instanceof String) {
      return ((String) value).length() < lengthLimit;
    } else if (value instanceof Value) {
      return isValidLengthValue((Value<?>) value, lengthLimit);
    }
    return true;
  }

  private static boolean isValidLengthValue(Value<?> value, int lengthLimit) {
    ValueType type = value.getType();
    if (type == ValueType.STRING) {
      return ((String) value.getValue()).length() < lengthLimit;
    } else if (type == ValueType.BYTES) {
      ByteBuffer buffer = (ByteBuffer) value.getValue();
      return buffer.remaining() <= lengthLimit;
    } else if (type == ValueType.ARRAY) {
      @SuppressWarnings("unchecked")
      List<Value<?>> array = (List<Value<?>>) value.getValue();
      return allMatch(array, element -> isValidLengthValue(element, lengthLimit));
    } else if (type == ValueType.KEY_VALUE_LIST) {
      @SuppressWarnings("unchecked")
      List<KeyValue> kvList = (List<KeyValue>) value.getValue();
      return allMatch(kvList, kv -> isValidLengthValue(kv.getValue(), lengthLimit));
    }
    return true;
  }

  private static <T> boolean allMatch(Iterable<T> iterable, Predicate<T> predicate) {
    for (T value : iterable) {
      if (!predicate.test(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Apply the {@code lengthLimit} to the attribute {@code value}. Strings, byte arrays, and nested
   * values which exceed the length limit are truncated.
   *
   * <p>Applies to:
   *
   * <ul>
   *   <li>String values
   *   <li>Each string within an array of strings
   *   <li>String values within {@link Value} objects
   *   <li>Byte array values within {@link Value} objects
   *   <li>Recursively, each element in an array of {@link Value}s
   *   <li>Recursively, each value in a {@link Value} key-value list
   * </ul>
   */
  public static Object applyAttributeLengthLimit(Object value, int lengthLimit) {
    if (lengthLimit == Integer.MAX_VALUE) {
      return value;
    }
    if (value instanceof List) {
      List<?> values = (List<?>) value;
      List<Object> response = new ArrayList<>(values.size());
      for (Object entry : values) {
        response.add(applyAttributeLengthLimit(entry, lengthLimit));
      }
      return response;
    }
    if (value instanceof String) {
      String str = (String) value;
      return str.length() < lengthLimit ? value : str.substring(0, lengthLimit);
    }
    if (value instanceof Value) {
      return applyValueLengthLimit((Value<?>) value, lengthLimit);
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  private static Value<?> applyValueLengthLimit(Value<?> value, int lengthLimit) {
    ValueType type = value.getType();

    if (type == ValueType.STRING) {
      String str = (String) value.getValue();
      if (str.length() <= lengthLimit) {
        return value;
      }
      return Value.of(str.substring(0, lengthLimit));
    } else if (type == ValueType.BYTES) {
      ByteBuffer buffer = (ByteBuffer) value.getValue();
      int length = buffer.remaining();
      if (length <= lengthLimit) {
        return value;
      }
      byte[] truncated = new byte[lengthLimit];
      buffer.get(truncated);
      return Value.of(truncated);
    } else if (type == ValueType.ARRAY) {
      List<Value<?>> array = (List<Value<?>>) value.getValue();
      boolean allValidLength = allMatch(array, element -> isValidLengthValue(element, lengthLimit));
      if (allValidLength) {
        return value;
      }
      List<Value<?>> result = new ArrayList<>(array.size());
      for (Value<?> element : array) {
        result.add(applyValueLengthLimit(element, lengthLimit));
      }
      return Value.of(result);
    } else if (type == ValueType.KEY_VALUE_LIST) {
      List<KeyValue> kvList = (List<KeyValue>) value.getValue();
      boolean allValidLength =
          allMatch(kvList, kv -> isValidLengthValue(kv.getValue(), lengthLimit));
      if (allValidLength) {
        return value;
      }
      List<KeyValue> result = new ArrayList<>(kvList.size());
      for (KeyValue kv : kvList) {
        result.add(KeyValue.of(kv.getKey(), applyValueLengthLimit(kv.getValue(), lengthLimit)));
      }
      return Value.of(result.toArray(new KeyValue[0]));
    }

    // For BOOLEAN, LONG, DOUBLE - no truncation needed
    return value;
  }
}
