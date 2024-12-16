/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

public interface ValueBuilder {

  Value<?> build();

  ValueBuilder put(String key, Value<?> value);

  default ValueBuilder put(String key, String value) {
    put(key, Value.of(value));
    return this;
  }

  default ValueBuilder put(String key, long value) {
    put(key, Value.of(value));
    return this;
  }

  default ValueBuilder put(String key, double value) {
    put(key, Value.of(value));
    return this;
  }

  default ValueBuilder put(String key, boolean value) {
    put(key, Value.of(value));
    return this;
  }
}
