/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

public interface ValueMapBuilder {

  default ValueMapBuilder put(String key, String value) {
    put(key, Value.of(value));
    return this;
  }

  default ValueMapBuilder put(String key, long value) {
    put(key, Value.of(value));
    return this;
  }

  default ValueMapBuilder put(String key, double value) {
    put(key, Value.of(value));
    return this;
  }

  default ValueMapBuilder put(String key, boolean value) {
    put(key, Value.of(value));
    return this;
  }

  ValueMapBuilder put(String key, Value<?> value);

  Value<?> build();
}
