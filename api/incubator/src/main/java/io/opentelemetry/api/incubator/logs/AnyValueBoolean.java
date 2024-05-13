/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import java.util.Objects;

final class AnyValueBoolean implements AnyValue<Boolean> {

  private final boolean value;

  private AnyValueBoolean(boolean value) {
    this.value = value;
  }

  static AnyValue<Boolean> create(boolean value) {
    return new AnyValueBoolean(value);
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.BOOLEAN;
  }

  @Override
  public Boolean getValue() {
    return value;
  }

  @Override
  public String asString() {
    return String.valueOf(value);
  }

  @Override
  public String toString() {
    return "AnyValueBoolean{" + asString() + "}";
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
    return Boolean.hashCode(value);
  }
}
