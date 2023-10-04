/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class AnyValueArray implements AnyValue<List<AnyValue<?>>> {

  private final List<AnyValue<?>> value;

  private AnyValueArray(List<AnyValue<?>> value) {
    this.value = value;
  }

  static AnyValue<List<AnyValue<?>>> create(AnyValue<?>... value) {
    Objects.requireNonNull(value, "value");
    List<AnyValue<?>> list = new ArrayList<>(value.length);
    list.addAll(Arrays.asList(value));
    return new AnyValueArray(Collections.unmodifiableList(list));
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.ARRAY;
  }

  @Override
  public List<AnyValue<?>> getValue() {
    return value;
  }

  @Override
  public String asString() {
    return value.stream().map(AnyValue::asString).collect(joining(", ", "[", "]"));
  }

  @Override
  public String toString() {
    return "AnyValueArray{" + asString() + "}";
  }
}
