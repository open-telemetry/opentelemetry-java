/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

final class ValueEmpty implements Value<Empty> {

  private static final ValueEmpty INSTANCE = new ValueEmpty();

  private ValueEmpty() {}

  static Value<Empty> create() {
    return INSTANCE;
  }

  @Override
  public ValueType getType() {
    return ValueType.EMPTY;
  }

  @Override
  public Empty getValue() {
    return Empty.getInstance();
  }

  @Override
  public String asString() {
    return "null";
  }

  @Override
  public String toString() {
    return "ValueEmpty{}";
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ValueEmpty;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
