/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

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
}
