/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.arrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.bytesKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.mapKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test for Attributes with complex attribute types. */
class ComplexAttributesTest {

  @Test
  void asMap() {
    byte[] data = "value".getBytes(UTF_8);
    List<Value<?>> list = Arrays.asList(Value.of("value1"), Value.of(100L));
    Attributes map = Attributes.builder()
        .put(stringKey("key"), "value")
        .build();

    Attributes attributes =
        Attributes.of(
            bytesKey("binary"), data,
            arrayKey("list"), list,
            mapKey("map"), map,
            stringKey("string"), "value1");

    Map<AttributeKey<?>, Object> asMap = attributes.asMap();
    assertThat(asMap.get(bytesKey("binary"))).isEqualTo(data);
    assertThat(asMap.get(arrayKey("list"))).isEqualTo(list);
    assertThat(asMap.get(mapKey("map"))).isEqualTo(map);
    assertThat(asMap.containsKey(bytesKey("binary"))).isTrue();
  }

  @Test
  void builder() {
    AttributeKey<byte[]> binaryKey = bytesKey("binary");
    AttributeKey<List<Value<?>>> listKey = arrayKey("list");
    AttributeKey<Attributes> mapKey = mapKey("map");

    byte[] data = "value".getBytes(UTF_8);
    List<Value<?>> list = Arrays.asList(Value.of("value1"), Value.of("value2"), Value.of(100L));
    Attributes map = Attributes.builder()
        .put(stringKey("key"), "value")
        .put(longKey("number"), 100L)
        .put(booleanKey("bool"), true)
        .build();

    Attributes attributes =
        Attributes.builder()
            .put(binaryKey, data)
            .put(listKey, list)
            .put(mapKey, map)
            .put(stringKey("string"), "value1")
            .build();

    // Attribute that was not set returns null.
    assertThat(attributes.get(longKey("long"))).isNull();
    // Attribute that was not set is missing from asMap.
    assertThat(attributes.asMap()).doesNotContainKey(longKey("long"));
    // Attribute that was set returns value.
    assertThat(attributes.get(stringKey("string"))).isEqualTo("value1");

    // Verify the complex attribute types.
    assertThat(binaryKey.getType()).isEqualTo(AttributeType.BYTES);
    assertThat(listKey.getType()).isEqualTo(AttributeType.ARRAY);
    assertThat(mapKey.getType()).isEqualTo(AttributeType.MAP);
  }

  @Test
  void get() {
    byte[] data = "test".getBytes(UTF_8);
    List<Value<?>> list = Arrays.asList(Value.of("a"), Value.of(1L));
    Attributes map = Attributes.builder()
        .put(stringKey("key"), "value")
        .build();

    Attributes attributes =
        Attributes.of(
            bytesKey("binary"), data,
            arrayKey("list"), list,
            mapKey("map"), map,
            stringKey("string"), "value");

    assertThat(attributes.get(bytesKey("binary"))).isEqualTo(data);
    assertThat(attributes.get(arrayKey("list"))).isEqualTo(list);
    assertThat(attributes.get(mapKey("map"))).isEqualTo(map);
    assertThat(attributes.get(stringKey("string"))).isEqualTo("value");
  }

  @Test
  void attributesToString() {
    List<Value<?>> list = Arrays.asList(Value.of("value1"));
    Attributes map = Attributes.builder()
        .put(stringKey("key"), "value")
        .build();

    Attributes attributes =
        Attributes.builder()
            .put(arrayKey("list"), list)
            .put(mapKey("map"), map)
            .build();

    // TODO revisit toString format for lists
    assertThat(attributes.toString()).isEqualTo("{list=[ValueString{value1}], map={key=\"value\"}}");
  }
}
