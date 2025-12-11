/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExtendedAttributesValueTest {

  @Test
  void put_ByteArrayTruncation() {
    ExtendedAttributesMap map = ExtendedAttributesMap.create(128, 5);
    byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    Value<ByteBuffer> value = Value.of(bytes);

    map.put(ExtendedAttributeKey.valueKey("key"), value);

    Value<?> result = map.get(ExtendedAttributeKey.valueKey("key"));
    ByteBuffer buffer = (ByteBuffer) result.getValue();
    byte[] resultBytes = new byte[buffer.remaining()];
    buffer.get(resultBytes);
    assertThat(resultBytes).containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  void put_ValueArrayTruncation() {
    ExtendedAttributesMap map = ExtendedAttributesMap.create(128, 5);

    Value<?> arrayValue = Value.of(Value.of("short"), Value.of("this is too long"));

    map.put(ExtendedAttributeKey.valueKey("key"), arrayValue);

    Value<?> result = map.get(ExtendedAttributeKey.valueKey("key"));
    @SuppressWarnings("unchecked")
    List<Value<?>> resultList = (List<Value<?>>) result.getValue();
    assertThat(resultList).hasSize(2);
    assertThat(resultList.get(0).getValue()).isEqualTo("short");
    assertThat(resultList.get(1).getValue()).isEqualTo("this ");
  }

  @Test
  void put_ValueKeyValueListTruncation() {
    ExtendedAttributesMap map = ExtendedAttributesMap.create(128, 5);

    Value<?> kvListValue =
        Value.of(
            KeyValue.of("key1", Value.of("short")),
            KeyValue.of("key2", Value.of("this is too long")));

    map.put(ExtendedAttributeKey.valueKey("key"), kvListValue);

    Value<?> result = map.get(ExtendedAttributeKey.valueKey("key"));
    @SuppressWarnings("unchecked")
    List<KeyValue> resultList = (List<KeyValue>) result.getValue();
    assertThat(resultList).hasSize(2);
    assertThat(resultList.get(0).getValue().getValue()).isEqualTo("short");
    assertThat(resultList.get(1).getValue().getValue()).isEqualTo("this ");
  }
}
