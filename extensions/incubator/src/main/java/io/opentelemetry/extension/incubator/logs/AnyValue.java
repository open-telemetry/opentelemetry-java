/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import java.util.List;
import java.util.Map;

public interface AnyValue<T> {

  static AnyValue<String> ofString(String value) {
    return AnyValueString.create(value);
  }

  static AnyValue<Boolean> ofBoolean(boolean value) {
    return AnyValueBoolean.create(value);
  }

  static AnyValue<Long> ofLong(long value) {
    return AnyValueLong.create(value);
  }

  static AnyValue<Double> ofDouble(double value) {
    return AnyValueDouble.create(value);
  }

  static AnyValue<byte[]> ofBytes(byte[] value) {
    return AnyValueBytes.create(value);
  }

  static AnyValue<List<AnyValue<?>>> ofArray(AnyValue<?>... value) {
    return AnyValueArray.create(value);
  }

  static AnyValue<List<KeyAnyValue>> ofKeyAnyValueArray(KeyAnyValue... value) {
    return KeyAnyValueList.create(value);
  }

  static AnyValue<List<KeyAnyValue>> ofMap(Map<String, AnyValue<?>> value) {
    return KeyAnyValueList.createFromMap(value);
  }

  AnyValueType getType();

  T getValue();

  String asString();
}
