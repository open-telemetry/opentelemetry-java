/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

public interface KeyAnyValue {

  static KeyAnyValue of(String key, AnyValue<?> value) {
    return KeyAnyValueImpl.create(key, value);
  }

  String getKey();

  AnyValue<?> getAnyValue();
}
