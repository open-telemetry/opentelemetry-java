/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Objects;

final class ValueString implements Value<String> {

  private final String value;

  private ValueString(String value) {
    this.value = value;
  }

  static Value<String> create(String value) {
    Objects.requireNonNull(value, "value must not be null");
    return new ValueString(value);
  }

  @Override
  public ValueType getType() {
    return ValueType.STRING;
  }

  @Override
  public String getValue() {
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
    return "ValueString{" + value + "}";
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
    return value.hashCode();
  }
}
