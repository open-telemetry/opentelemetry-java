/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class KeyValueList implements Value<List<KeyValue>> {

  private final List<KeyValue> value;

  private KeyValueList(List<KeyValue> value) {
    this.value = value;
  }

  static Value<List<KeyValue>> create(KeyValue... value) {
    Objects.requireNonNull(value, "value must not be null");
    List<KeyValue> list = new ArrayList<>(value.length);
    list.addAll(Arrays.asList(value));
    return new KeyValueList(Collections.unmodifiableList(list));
  }

  static Value<List<KeyValue>> createFromMap(Map<String, Value<?>> value) {
    Objects.requireNonNull(value, "value must not be null");
    KeyValue[] array =
        value.entrySet().stream()
            .map(entry -> KeyValue.of(entry.getKey(), entry.getValue()))
            .toArray(KeyValue[]::new);
    return create(array);
  }

  @Override
  public ValueType getType() {
    return ValueType.KEY_VALUE_LIST;
  }

  @Override
  public List<KeyValue> getValue() {
    return value;
  }

  @Override
  public String asString() {
    StringBuilder sb = new StringBuilder();
    ProtoJson.append(sb, this);
    return sb.toString();
  }

  @Override
  public String toString() {
    return "KeyValueList{" + asString() + "}";
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
