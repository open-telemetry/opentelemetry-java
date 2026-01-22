/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Objects;

final class ValueDouble implements Value<Double> {

  private final double value;

  private ValueDouble(double value) {
    this.value = value;
  }

  static Value<Double> create(double value) {
    return new ValueDouble(value);
  }

  @Override
  public ValueType getType() {
    return ValueType.DOUBLE;
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public String asString() {
    if (Double.isNaN(value)) {
      return "NaN";
    } else if (Double.isInfinite(value)) {
      return value > 0 ? "Infinity" : "-Infinity";
    }
    return String.valueOf(value);
  }

  @Override
  public String toString() {
    return "ValueDouble{" + asString() + "}";
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
    return Double.hashCode(value);
  }
}
