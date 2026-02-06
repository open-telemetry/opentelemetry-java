/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.nio.ByteBuffer;
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
 *   <li>An empty value via {@link #empty()}
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
   * Returns an empty {@link Value}.
   *
   * @since 1.59.0
   */
  static Value<Empty> empty() {
    return ValueEmpty.create();
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
   *   <li>{@link ValueType#EMPTY} returns {@link Empty}
   * </ul>
   */
  T getValue();

  /**
   * Returns a string representation of this {@link Value}.
   *
   * <p>The output follows the <a
   * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/common/README.md#anyvalue-representation-for-non-otlp-protocols">
   * string representation guidance</a> for complex attribute value types:
   *
   * <ul>
   *   <li>{@link ValueType#STRING} String as-is without surrounding quotes. Examples: {@code hello
   *       world}, (empty string)
   *   <li>{@link ValueType#BOOLEAN} JSON boolean. Examples: {@code true}, {@code false}
   *   <li>{@link ValueType#LONG} JSON number. Examples: {@code 42}, {@code -123}
   *   <li>{@link ValueType#DOUBLE} JSON number, or {@code NaN}, {@code Infinity}, {@code -Infinity}
   *       for special values (without surrounding quotes). Examples: {@code 3.14159}, {@code
   *       1.23e10}, {@code NaN}, {@code -Infinity}
   *   <li>{@link ValueType#ARRAY} JSON array. Nested byte arrays are encoded as Base64-encoded JSON
   *       strings. Nested empty values are encoded as JSON {@code null}. The special floating point
   *       values NaN and Infinity are encoded as JSON strings {@code "NaN"}, {@code "Infinity"},
   *       and {@code "-Infinity"}. Examples: {@code []}, {@code [1, "-Infinity", "a", true,
   *       {"nested": "aGVsbG8gd29ybGQ="}]}
   *   <li>{@link ValueType#KEY_VALUE_LIST} JSON object. Nested byte arrays are encoded as
   *       Base64-encoded JSON strings. Nested empty values are encoded as JSON {@code null}. The
   *       special floating point values NaN and Infinity are encoded as JSON strings {@code "NaN"},
   *       {@code "Infinity"}, and {@code "-Infinity"}. Examples: {@code {}}, {@code {"a":
   *       "-Infinity", "b": 2, "c": [3, null]}}
   *   <li>{@link ValueType#BYTES} Base64-encoded bytes without surrounding quotes. Example: {@code
   *       aGVsbG8gd29ybGQ=}
   *   <li>{@link ValueType#EMPTY} The empty string.
   * </ul>
   *
   * @return a string representation of this value
   */
  String asString();
}
