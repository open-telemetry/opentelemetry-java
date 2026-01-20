/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class ValueArray implements Value<List<Value<?>>> {

  private final List<Value<?>> value;

  private ValueArray(List<Value<?>> value) {
    this.value = value;
  }

  static Value<List<Value<?>>> create(Value<?>... value) {
    Objects.requireNonNull(value, "value must not be null");
    List<Value<?>> list = new ArrayList<>(value.length);
    list.addAll(Arrays.asList(value));
    return new ValueArray(Collections.unmodifiableList(list));
  }

  static Value<List<Value<?>>> create(List<Value<?>> value) {
    return new ValueArray(Collections.unmodifiableList(value));
  }

  @Override
  public ValueType getType() {
    return ValueType.ARRAY;
  }

  @Override
  public List<Value<?>> getValue() {
    return value;
  }

  @Override
  public String asString() {
    StringBuilder sb = new StringBuilder();
    ProtoJson.append(sb, this);
    return sb.toString();
  }

  @Override
  public String toString() {
    return "ValueArray{" + asString() + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof Value) && Objects.equals(this.value, ((Value<?>) o).getValue());
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
