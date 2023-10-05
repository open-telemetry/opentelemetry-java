/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import java.util.List;
import java.util.Map;

public interface AnyValue<T> {

  static AnyValue<String> of(String value) {
    return AnyValueString.create(value);
  }

  static AnyValue<Boolean> of(boolean value) {
    return AnyValueBoolean.create(value);
  }

  static AnyValue<Long> of(long value) {
    return AnyValueLong.create(value);
  }

  static AnyValue<Double> of(double value) {
    return AnyValueDouble.create(value);
  }

  static AnyValue<byte[]> of(byte[] value) {
    return AnyValueBytes.create(value);
  }

  static AnyValue<List<AnyValue<?>>> of(AnyValue<?>... value) {
    return AnyValueArray.create(value);
  }

  static AnyValue<List<KeyAnyValue>> of(KeyAnyValue... value) {
    return KeyAnyValueList.create(value);
  }

  static AnyValue<List<KeyAnyValue>> of(Map<String, AnyValue<?>> value) {
    return KeyAnyValueList.createFromMap(value);
  }

  AnyValueType getType();

  T getValue();

  String asString();
}
