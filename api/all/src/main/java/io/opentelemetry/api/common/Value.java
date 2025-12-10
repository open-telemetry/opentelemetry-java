/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Value mirrors the proto <a
 * href="https://github.com/open-telemetry/opentelemetry-proto/blob/ac3242b03157295e4ee9e616af53b81517b06559/opentelemetry/proto/common/v1/common.proto#L28">AnyValue</a>
 * message type, and is used to model any type.
 *
 * <p>It can be used to represent:
 *
 * <ul>
 *   <li>Primitive values via {@link #of(long)}, {@link #of(String)}, {@link #of(boolean)}, {@link
 *       #of(double)}.
 *   <li>String-keyed maps (i.e. associative arrays, dictionaries) via {@link #of(KeyValue...)},
 *       {@link #of(Map)}. Note, because map values are type {@link Value}, maps can be nested
 *       within other maps.
 *   <li>Arrays (heterogeneous or homogenous) via {@link #of(Value[])}. Note, because array values
 *       are type {@link Value}, arrays can contain primitives, complex types like maps or arrays,
 *       or any combination.
 *   <li>Raw bytes via {@link #of(byte[])}
 * </ul>
 *
 * <p>Currently, Value is only used as an argument for {@link
 * io.opentelemetry.api.logs.LogRecordBuilder#setBody(Value)}.
 *
 * @param <T> the type. See {@link #getValue()} for description of types.
 * @since 1.42.0
 */
public interface Value<T> {

  /** Returns an {@link Value} for the {@link String} value. */
  static Value<String> of(String value) {
    return ValueString.create(value);
  }

  /** Returns an {@link Value} for the {@code boolean} value. */
  static Value<Boolean> of(boolean value) {
    return ValueBoolean.create(value);
  }

  /** Returns an {@link Value} for the {@code long} value. */
  static Value<Long> of(long value) {
    return ValueLong.create(value);
  }

  /** Returns an {@link Value} for the {@code double} value. */
  static Value<Double> of(double value) {
    return ValueDouble.create(value);
  }

  /** Returns an {@link Value} for the {@code byte[]} value. */
  static Value<ByteBuffer> of(byte[] value) {
    return ValueBytes.create(value);
  }

  /** Returns an {@link Value} for the array of {@link Value} values. */
  static Value<List<Value<?>>> of(Value<?>... value) {
    return ValueArray.create(value);
  }

  /** Returns an {@link Value} for the list of {@link Value} values. */
  static Value<List<Value<?>>> of(List<Value<?>> value) {
    return ValueArray.create(value);
  }

  /**
   * Returns an {@link Value} for the array of {@link KeyValue} values. {@link KeyValue#getKey()}
   * values should not repeat - duplicates may be dropped.
   */
  static Value<List<KeyValue>> of(KeyValue... value) {
    return KeyValueList.create(value);
  }

  /** Returns an {@link Value} for the {@link Map} of key, {@link Value}. */
  static Value<List<KeyValue>> of(Map<String, Value<?>> value) {
    return KeyValueList.createFromMap(value);
  }

  /**
   * Convert an object of primitives, Lists, and Maps (nested in any manner) to {@link Value}.
   *
   * <p>Specifically, the following types are supported. If the {@code object} (or any nested data
   * structures) contains an unsupported type, an {@link IllegalArgumentException} is thrown.
   *
   * <ul>
   *   <li>{@link Long}
   *   <li>{@link Integer}
   *   <li>{@link Float}
   *   <li>{@link Double}
   *   <li>{@link Boolean}
   *   <li>{@code byte[]}
   *   <li>{@code List<?>}, where each list entry is a supported type
   *   <li>{@code Map<String, ?>}, where each value is a supported type
   * </ul>
   *
   * @param object the object to convert
   * @return the equivalent {@link Value}
   * @throws IllegalArgumentException if not able to convert the object to {@link Value}
   */
  @SuppressWarnings("ThrowsUncheckedException")
  static Value<?> convert(Object object) throws IllegalArgumentException {
    if (object instanceof Integer) {
      return Value.of((Integer) object);
    }
    if (object instanceof Long) {
      return Value.of((Long) object);
    }
    if (object instanceof Float) {
      return Value.of((Float) object);
    }
    if (object instanceof Double) {
      return Value.of((Double) object);
    }
    if (object instanceof Boolean) {
      return Value.of((Boolean) object);
    }
    if (object instanceof String) {
      return Value.of((String) object);
    }
    if (object instanceof byte[]) {
      return Value.of((byte[]) object);
    }
    if (object instanceof List) {
      List<?> list = (List<?>) object;
      List<Value<?>> valueList = new ArrayList<>(list.size());
      for (Object entry : list) {
        valueList.add(Value.convert(entry));
      }
      return Value.of(valueList);
    }
    if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) object;
      Map<String, Value<?>> valueMap = new HashMap<>(map.size());
      map.forEach(
          (key, value) -> {
            if (!(key instanceof String)) {
              throw new IllegalArgumentException(
                  "Cannot convert map with key type "
                      + key.getClass().getSimpleName()
                      + " to value");
            }
            valueMap.put((String) key, Value.convert(value));
          });
      return Value.of(valueMap);
    }
    throw new IllegalArgumentException(
        "Cannot convert object of type " + object.getClass().getSimpleName() + " to value");
  }

  /** Returns the type of this {@link Value}. Useful for building switch statements. */
  ValueType getType();

  /**
   * Returns the value for this {@link Value}.
   *
   * <p>The return type varies by {@link #getType()} as described below:
   *
   * <ul>
   *   <li>{@link ValueType#STRING} returns {@link String}
   *   <li>{@link ValueType#BOOLEAN} returns {@code boolean}
   *   <li>{@link ValueType#LONG} returns {@code long}
   *   <li>{@link ValueType#DOUBLE} returns {@code double}
   *   <li>{@link ValueType#ARRAY} returns {@link List} of {@link Value}
   *   <li>{@link ValueType#KEY_VALUE_LIST} returns {@link List} of {@link KeyValue}
   *   <li>{@link ValueType#BYTES} returns read only {@link ByteBuffer}. See {@link
   *       ByteBuffer#asReadOnlyBuffer()}.
   * </ul>
   */
  T getValue();

  /**
   * Return a string encoding of this {@link Value}. This is intended to be a fallback serialized
   * representation in case there is no suitable encoding that can utilize {@link #getType()} /
   * {@link #getValue()} to serialize specific types.
   *
   * <p>WARNING: No guarantees are made about the encoding of this string response. It MAY change in
   * a future minor release. If you need a reliable string encoding, write your own serializer.
   */
  // TODO(jack-berg): Should this be a JSON encoding?
  String asString();
}
