/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

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
}
