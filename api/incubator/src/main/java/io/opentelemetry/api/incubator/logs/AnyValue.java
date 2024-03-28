/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * AnyValue mirrors the proto <a
 * href="https://github.com/open-telemetry/opentelemetry-proto/blob/ac3242b03157295e4ee9e616af53b81517b06559/opentelemetry/proto/common/v1/common.proto#L28">AnyValue</a>
 * message type, and is used to model any type.
 *
 * <p>It can be used to represent:
 *
 * <ul>
 *   <li>Primitive values via {@link #of(long)}, {@link #of(String)}, {@link #of(boolean)}, {@link
 *       #of(double)}.
 *   <li>String-keyed maps (i.e. associative arrays, dictionaries) via {@link #of(KeyAnyValue...)},
 *       {@link #of(Map)}. Note, because map values are type {@link AnyValue}, maps can be nested
 *       within other maps.
 *   <li>Arrays (heterogeneous or homogenous) via {@link #of(AnyValue[])}. Note, because array
 *       values are type {@link AnyValue}, arrays can contain primitives, complex types like maps or
 *       arrays, or any combination.
 *   <li>Raw bytes via {@link #of(byte[])}
 * </ul>
 *
 * @param <T> the type. See {@link #getValue()} for description of types.
 */
public interface AnyValue<T> {

  /** Returns an {@link AnyValue} for the {@link String} value. */
  static AnyValue<String> of(String value) {
    return AnyValueString.create(value);
  }

  /** Returns an {@link AnyValue} for the {@code boolean} value. */
  static AnyValue<Boolean> of(boolean value) {
    return AnyValueBoolean.create(value);
  }

  /** Returns an {@link AnyValue} for the {@code long} value. */
  static AnyValue<Long> of(long value) {
    return AnyValueLong.create(value);
  }

  /** Returns an {@link AnyValue} for the {@code double} value. */
  static AnyValue<Double> of(double value) {
    return AnyValueDouble.create(value);
  }

  /** Returns an {@link AnyValue} for the {@code byte[]} value. */
  static AnyValue<ByteBuffer> of(byte[] value) {
    return AnyValueBytes.create(value);
  }

  /** Returns an {@link AnyValue} for the array of {@link AnyValue} values. */
  static AnyValue<List<AnyValue<?>>> of(AnyValue<?>... value) {
    return AnyValueArray.create(value);
  }

  /** Returns an {@link AnyValue} for the list of {@link AnyValue} values. */
  static AnyValue<List<AnyValue<?>>> of(List<AnyValue<?>> value) {
    return AnyValueArray.create(value);
  }

  /**
   * Returns an {@link AnyValue} for the array of {@link KeyAnyValue} values. {@link
   * KeyAnyValue#getKey()} values should not repeat - duplicates may be dropped.
   */
  static AnyValue<List<KeyAnyValue>> of(KeyAnyValue... value) {
    return KeyAnyValueList.create(value);
  }

  /** Returns an {@link AnyValue} for the {@link Map} of key, {@link AnyValue}. */
  static AnyValue<List<KeyAnyValue>> of(Map<String, AnyValue<?>> value) {
    return KeyAnyValueList.createFromMap(value);
  }

  /** Returns the type of this {@link AnyValue}. Useful for building switch statements. */
  AnyValueType getType();

  /**
   * Returns the value for this {@link AnyValue}.
   *
   * <p>The return type varies by {@link #getType()} as described below:
   *
   * <ul>
   *   <li>{@link AnyValueType#STRING} returns {@link String}
   *   <li>{@link AnyValueType#BOOLEAN} returns {@code boolean}
   *   <li>{@link AnyValueType#LONG} returns {@code long}
   *   <li>{@link AnyValueType#DOUBLE} returns {@code double}
   *   <li>{@link AnyValueType#ARRAY} returns {@link List} of {@link AnyValue}
   *   <li>{@link AnyValueType#KEY_VALUE_LIST} returns {@link List} of {@link KeyAnyValue}
   *   <li>{@link AnyValueType#BYTES} returns read only {@link ByteBuffer}. See {@link
   *       ByteBuffer#asReadOnlyBuffer()}.
   * </ul>
   */
  T getValue();

  /**
   * Return a string encoding of this {@link AnyValue}. This is intended to be a fallback serialized
   * representation in case there is no suitable encoding that can utilize {@link #getType()} /
   * {@link #getValue()} to serialize specific types.
   */
  // TODO(jack-berg): Should this be a JSON encoding?
  String asString();
}
