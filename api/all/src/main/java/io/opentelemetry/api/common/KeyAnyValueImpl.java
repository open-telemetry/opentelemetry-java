/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class KeyAnyValueImpl implements KeyAnyValue {

  KeyAnyValueImpl() {}

  static KeyAnyValueImpl create(String key, AnyValue<?> value) {
    return new AutoValue_KeyAnyValueImpl(key, value);
  }
}
