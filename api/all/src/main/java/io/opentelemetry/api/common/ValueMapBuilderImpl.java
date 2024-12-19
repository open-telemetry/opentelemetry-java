/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.List;

class ValueMapBuilderImpl implements ValueMapBuilder {

  private final List<KeyValue> keyValues = new ArrayList<>();

  @Override
  public ValueMapBuilder put(String key, Value<?> value) {
    keyValues.add(KeyValue.of(key, value));
    return this;
  }

  @Override
  public Value<?> build() {
    return Value.of(keyValues.toArray(new KeyValue[0]));
  }
}
