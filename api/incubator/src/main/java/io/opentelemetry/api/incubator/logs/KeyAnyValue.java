/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

/**
 * Key-value pair of {@link String} key and {@link AnyValue} value.
 *
 * @see AnyValue#of(KeyAnyValue...)
 */
public interface KeyAnyValue {

  /** Returns a {@link KeyAnyValue} for the given {@code key} and {@code value}. */
  static KeyAnyValue of(String key, AnyValue<?> value) {
    return KeyAnyValueImpl.create(key, value);
  }

  /** Returns the key. */
  String getKey();

  /** Returns the value. */
  AnyValue<?> getAnyValue();
}
