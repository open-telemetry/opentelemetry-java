/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.List;

class ValueListBuilderImpl implements ValueListBuilder {

  private final List<Value<?>> values = new ArrayList<>();

  @Override
  public ValueListBuilder add(Value<?> value) {
    values.add(value);
    return this;
  }

  @Override
  public Value<?> build() {
    return Value.of(values.toArray(new Value<?>[0]));
  }
}
