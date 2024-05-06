/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import java.util.Objects;

final class AnyValueDouble implements AnyValue<Double> {

  private final double value;

  private AnyValueDouble(double value) {
    this.value = value;
  }

  static AnyValue<Double> create(double value) {
    return new AnyValueDouble(value);
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.DOUBLE;
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public String asString() {
    return String.valueOf(value);
  }

  @Override
  public String toString() {
    return "AnyValueDouble{" + asString() + "}";
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
    return Double.hashCode(value);
  }
}
