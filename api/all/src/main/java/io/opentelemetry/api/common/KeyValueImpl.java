/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class KeyValueImpl implements KeyValue {

  KeyValueImpl() {}

  static KeyValueImpl create(String key, Value<?> value) {
    return new AutoValue_KeyValueImpl(key, value);
  }
}
