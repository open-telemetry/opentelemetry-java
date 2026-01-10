/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Objects;

final class ValueLong implements Value<Long> {

  private final long value;

  private ValueLong(long value) {
    this.value = value;
  }

  static Value<Long> create(long value) {
    return new ValueLong(value);
  }

  @Override
  public ValueType getType() {
    return ValueType.LONG;
  }

  @Override
  public Long getValue() {
    return value;
  }

  @Override
  public String asString() {
    StringBuilder sb = new StringBuilder();
    JsonUtil.appendJsonValue(sb, this);
    return sb.toString();
  }

  @Override
  public String toString() {
    return "ValueLong{" + asString() + "}";
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
    return Long.hashCode(value);
  }
}
