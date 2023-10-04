/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

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
    return AnyValueType.LONG;
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
}
