/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValueTest {

  @Test
  void value_OfString() {
    assertThat(Value.of("foo"))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.STRING);
              assertThat(value.getValue()).isEqualTo("foo");
              assertThat(value).hasSameHashCodeAs(Value.of("foo"));
            });
  }

  @Test
  void value_OfBoolean() {
    assertThat(Value.of(true))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.BOOLEAN);
              assertThat(value.getValue()).isEqualTo(true);
              assertThat(value).hasSameHashCodeAs(Value.of(true));
            });
  }

  @Test
  void value_OfLong() {
    assertThat(Value.of(1L))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.LONG);
              assertThat(value.getValue()).isEqualTo(1L);
              assertThat(value).hasSameHashCodeAs(Value.of(1L));
            });
  }

  @Test
  void value_OfDouble() {
    assertThat(Value.of(1.1))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.DOUBLE);
              assertThat(value.getValue()).isEqualTo(1.1);
              assertThat(value).hasSameHashCodeAs(Value.of(1.1));
            });
  }

  @Test
  void value_OfByteArray() {
    assertThat(Value.of(new byte[] {'a', 'b'}))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.BYTES);
              ByteBuffer buf = value.getValue();
              // ValueBytes returns read only view of ByteBuffer
              assertThatThrownBy(buf::array).isInstanceOf(ReadOnlyBufferException.class);
              byte[] bytes = new byte[buf.remaining()];
              buf.get(bytes);
              assertThat(bytes).isEqualTo(new byte[] {'a', 'b'});
              assertThat(value).hasSameHashCodeAs(Value.of(new byte[] {'a', 'b'}));
            });
  }

  @Test
  void value_OfvalueArray() {
    assertThat(Value.of(Value.of(true), Value.of(1L)))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.ARRAY);
              assertThat(value.getValue()).isEqualTo(Arrays.asList(Value.of(true), Value.of(1L)));
              assertThat(value).hasSameHashCodeAs(Value.of(Value.of(true), Value.of(1L)));
            });
  }

  @Test
  @SuppressWarnings("DoubleBraceInitialization")
  void value_OfKeyValueList() {
    assertThat(Value.of(KeyValue.of("bool", Value.of(true)), KeyValue.of("long", Value.of(1L))))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.KEY_VALUE_LIST);
              assertThat(value.getValue())
                  .isEqualTo(
                      Arrays.asList(
                          KeyValue.of("bool", Value.of(true)), KeyValue.of("long", Value.of(1L))));
              assertThat(value)
                  .hasSameHashCodeAs(
                      Value.of(
                          KeyValue.of("bool", Value.of(true)), KeyValue.of("long", Value.of(1L))));
            });

    assertThat(
            Value.of(
                new LinkedHashMap<String, Value<?>>() {
                  {
                    put("bool", Value.of(true));
                    put("long", Value.of(1L));
                  }
                }))
        .satisfies(
            value -> {
              assertThat(value.getType()).isEqualTo(ValueType.KEY_VALUE_LIST);
              assertThat(value.getValue())
                  .isEqualTo(
                      Arrays.asList(
                          KeyValue.of("bool", Value.of(true)), KeyValue.of("long", Value.of(1L))));
              assertThat(value)
                  .hasSameHashCodeAs(
                      Value.of(
                          new LinkedHashMap<String, Value<?>>() {
                            {
                              put("bool", Value.of(true));
                              put("long", Value.of(1L));
                            }
                          }));
            });
  }

  @Test
  void value_NullsNotAllowed() {
    assertThatThrownBy(() -> Value.of((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> Value.of((byte[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> Value.of((Value<?>[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> Value.of((KeyValue[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> Value.of((Map<String, Value<?>>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
  }

  @ParameterizedTest
  @MethodSource("asStringArgs")
  void asString(Value<?> value, String expectedAsString) {
    assertThat(value.asString()).isEqualTo(expectedAsString);
  }

  @SuppressWarnings("DoubleBraceInitialization")
  private static Stream<Arguments> asStringArgs() {
    return Stream.of(
        // primitives
        arguments(Value.of("str"), "\"str\""),
        arguments(Value.of(true), "true"),
        arguments(Value.of(1), "1"),
        arguments(Value.of(1.1), "1.1"),
        // heterogeneous array
        arguments(
            Value.of(Value.of("str"), Value.of(true), Value.of(1), Value.of(1.1)),
            "[\"str\",true,1,1.1]"),
        // key value list from KeyValue array
        arguments(
            Value.of(KeyValue.of("key1", Value.of("val1")), KeyValue.of("key2", Value.of(2))),
            "{\"key1\":\"val1\",\"key2\":2}"),
        // key value list from map
        arguments(
            Value.of(
                new LinkedHashMap<String, Value<?>>() {
                  {
                    put("key1", Value.of("val1"));
                    put("key2", Value.of(2));
                  }
                }),
            "{\"key1\":\"val1\",\"key2\":2}"),
        // map of map
        arguments(
            Value.of(
                Collections.singletonMap(
                    "child", Value.of(Collections.singletonMap("grandchild", Value.of("str"))))),
            "{\"child\":{\"grandchild\":\"str\"}}"),
        // bytes
        arguments(
            Value.of("hello world".getBytes(StandardCharsets.UTF_8)), "\"aGVsbG8gd29ybGQ=\""));
  }

  @Test
  void valueByteAsString() {
    // TODO: add more test cases
    String str = "hello world";
    String base64Encoded = Value.of(str.getBytes(StandardCharsets.UTF_8)).asString();
    // Remove surrounding quotes from JSON string
    String base64Value = base64Encoded.substring(1, base64Encoded.length() - 1);
    byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
    assertThat(new String(decodedBytes, StandardCharsets.UTF_8)).isEqualTo(str);
  }
}
