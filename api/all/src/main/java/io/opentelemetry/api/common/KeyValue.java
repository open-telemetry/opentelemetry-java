/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/**
 * Key-value pair of {@link String} key and {@link Value} value.
 *
 * @see Value#of(KeyValue...)
 */
public interface KeyValue {

  /** Returns a {@link KeyValue} for the given {@code key} and {@code value}. */
  static KeyValue of(String key, Value<?> value) {
    return KeyValueImpl.create(key, value);
  }

  /** Returns the key. */
  String getKey();

  /** Returns the value. */
  Value<?> getValue();
}
