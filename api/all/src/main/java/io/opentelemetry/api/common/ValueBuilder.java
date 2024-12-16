/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  default ValueBuilder put(String key, String... value) {
    put(key, Value.of(Arrays.stream(value).map(Value::of).collect(toList())));
    return this;
  }

  default ValueBuilder put(String key, long... value) {
    put(key, Value.of(Arrays.stream(value).mapToObj(Value::of).collect(toList())));
    return this;
  }

  default ValueBuilder put(String key, double... value) {
    put(key, Value.of(Arrays.stream(value).mapToObj(Value::of).collect(toList())));
    return this;
  }

  default ValueBuilder put(String key, boolean... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (boolean val : value) {
      values.add(Value.of(val));
    }
    put(key, Value.of(values));
    return this;
  }
}
