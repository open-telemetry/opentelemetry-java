/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import java.util.Objects;

final class AnyValueString implements AnyValue<String> {

  private final String value;

  private AnyValueString(String value) {
    this.value = value;
  }

  static AnyValue<String> create(String value) {
    Objects.requireNonNull(value, "value must not be null");
    return new AnyValueString(value);
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.STRING;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String asString() {
    return value;
  }

  @Override
  public String toString() {
    return "AnyValueString{" + value + "}";
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
    return value.hashCode();
  }
}
