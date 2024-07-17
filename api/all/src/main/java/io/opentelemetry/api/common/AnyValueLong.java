/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Objects;

final class AnyValueLong implements AnyValue<Long> {

  private final long value;

  private AnyValueLong(long value) {
    this.value = value;
  }

  static AnyValue<Long> create(long value) {
    return new AnyValueLong(value);
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.LONG;
  }

  @Override
  public Long getValue() {
    return value;
  }

  @Override
  public String asString() {
    return String.valueOf(value);
  }

  @Override
  public String toString() {
    return "AnyValueLong{" + asString() + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof AnyValue) && Objects.equals(this.value, ((AnyValue<?>) o).getValue());
  }

  @Override
  public int hashCode() {
    return Long.hashCode(value);
  }
}
