/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class KeyAnyValueList implements AnyValue<List<KeyAnyValue>> {

  private final List<KeyAnyValue> value;

  private KeyAnyValueList(List<KeyAnyValue> value) {
    this.value = value;
  }

  static AnyValue<List<KeyAnyValue>> create(KeyAnyValue... value) {
    Objects.requireNonNull(value, "value must not be null");
    List<KeyAnyValue> list = new ArrayList<>(value.length);
    list.addAll(Arrays.asList(value));
    return new KeyAnyValueList(Collections.unmodifiableList(list));
  }

  static AnyValue<List<KeyAnyValue>> createFromMap(Map<String, AnyValue<?>> value) {
    Objects.requireNonNull(value, "value must not be null");
    KeyAnyValue[] array =
        value.entrySet().stream()
            .map(entry -> KeyAnyValue.of(entry.getKey(), entry.getValue()))
            .toArray(KeyAnyValue[]::new);
    return create(array);
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.KEY_VALUE_LIST;
  }

  @Override
  public List<KeyAnyValue> getValue() {
    return value;
  }

  @Override
  public String asString() {
    return value.stream()
        .map(item -> item.getKey() + "=" + item.getAnyValue().asString())
        .collect(joining(", ", "[", "]"));
  }

  @Override
  public String toString() {
    return "KeyAnyValueList{" + asString() + "}";
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
