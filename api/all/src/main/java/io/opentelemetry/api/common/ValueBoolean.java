/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Objects;

final class ValueBoolean implements Value<Boolean> {

  private final boolean value;

  private ValueBoolean(boolean value) {
    this.value = value;
  }

  static Value<Boolean> create(boolean value) {
    return new ValueBoolean(value);
  }

  @Override
  public ValueType getType() {
    return ValueType.BOOLEAN;
  }

  @Override
  public Boolean getValue() {
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
    return "ValueBoolean{" + asString() + "}";
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
    return Boolean.hashCode(value);
  }
}
