/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

class AnyValueTest {

  @Test
  void anyValue_OfString() {
    assertThat(AnyValue.of("foo"))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.STRING);
              assertThat(anyValue.getValue()).isEqualTo("foo");
              assertThat(anyValue).hasSameHashCodeAs(AnyValue.of("foo"));
            });
  }

  @Test
  void anyValue_OfBoolean() {
    assertThat(AnyValue.of(true))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.BOOLEAN);
              assertThat(anyValue.getValue()).isEqualTo(true);
              assertThat(anyValue).hasSameHashCodeAs(AnyValue.of(true));
            });
  }

  @Test
  void anyValue_OfLong() {
    assertThat(AnyValue.of(1L))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.LONG);
              assertThat(anyValue.getValue()).isEqualTo(1L);
              assertThat(anyValue).hasSameHashCodeAs(AnyValue.of(1L));
            });
  }

  @Test
  void anyValue_OfDouble() {
    assertThat(AnyValue.of(1.1))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.DOUBLE);
              assertThat(anyValue.getValue()).isEqualTo(1.1);
              assertThat(anyValue).hasSameHashCodeAs(AnyValue.of(1.1));
            });
  }

  @Test
  void anyValue_OfByteArray() {
    assertThat(AnyValue.of(new byte[] {'a', 'b'}))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.BYTES);
              ByteBuffer value = anyValue.getValue();
              // AnyValueBytes returns read only view of ByteBuffer
              assertThatThrownBy(value::array).isInstanceOf(ReadOnlyBufferException.class);
              byte[] bytes = new byte[value.remaining()];
              value.get(bytes);
              assertThat(bytes).isEqualTo(new byte[] {'a', 'b'});
              assertThat(anyValue).hasSameHashCodeAs(AnyValue.of(new byte[] {'a', 'b'}));
            });
  }

  @Test
  void anyValue_OfAnyValueArray() {
    assertThat(AnyValue.of(AnyValue.of(true), AnyValue.of(1L)))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.ARRAY);
              assertThat(anyValue.getValue())
                  .isEqualTo(Arrays.asList(AnyValue.of(true), AnyValue.of(1L)));
              assertThat(anyValue)
                  .hasSameHashCodeAs(AnyValue.of(AnyValue.of(true), AnyValue.of(1L)));
            });
  }

  @Test
  @SuppressWarnings("DoubleBraceInitialization")
  void anyValue_OfKeyValueList() {
    assertThat(
            AnyValue.of(
                KeyAnyValue.of("bool", AnyValue.of(true)), KeyAnyValue.of("long", AnyValue.of(1L))))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.KEY_VALUE_LIST);
              assertThat(anyValue.getValue())
                  .isEqualTo(
                      Arrays.asList(
                          KeyAnyValue.of("bool", AnyValue.of(true)),
                          KeyAnyValue.of("long", AnyValue.of(1L))));
              assertThat(anyValue)
                  .hasSameHashCodeAs(
                      AnyValue.of(
                          KeyAnyValue.of("bool", AnyValue.of(true)),
                          KeyAnyValue.of("long", AnyValue.of(1L))));
            });

    assertThat(
            AnyValue.of(
                new LinkedHashMap<String, AnyValue<?>>() {
                  {
                    put("bool", AnyValue.of(true));
                    put("long", AnyValue.of(1L));
                  }
                }))
        .satisfies(
            anyValue -> {
              assertThat(anyValue.getType()).isEqualTo(AnyValueType.KEY_VALUE_LIST);
              assertThat(anyValue.getValue())
                  .isEqualTo(
                      Arrays.asList(
                          KeyAnyValue.of("bool", AnyValue.of(true)),
                          KeyAnyValue.of("long", AnyValue.of(1L))));
              assertThat(anyValue)
                  .hasSameHashCodeAs(
                      AnyValue.of(
                          new LinkedHashMap<String, AnyValue<?>>() {
                            {
                              put("bool", AnyValue.of(true));
                              put("long", AnyValue.of(1L));
                            }
                          }));
            });
  }

  @Test
  void anyValue_NullsNotAllowed() {
    assertThatThrownBy(() -> AnyValue.of((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> AnyValue.of((byte[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> AnyValue.of((AnyValue<?>[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> AnyValue.of((KeyAnyValue[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
    assertThatThrownBy(() -> AnyValue.of((Map<String, AnyValue<?>>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("value must not be null");
  }

  @ParameterizedTest
  @MethodSource("asStringArgs")
  void asString(AnyValue<?> value, String expectedAsString) {
    assertThat(value.asString()).isEqualTo(expectedAsString);
  }

  @SuppressWarnings("DoubleBraceInitialization")
  private static Stream<Arguments> asStringArgs() {
    return Stream.of(
        // primitives
        arguments(AnyValue.of("str"), "str"),
        arguments(AnyValue.of(true), "true"),
        arguments(AnyValue.of(1), "1"),
        arguments(AnyValue.of(1.1), "1.1"),
        // heterogeneous array
        arguments(
            AnyValue.of(AnyValue.of("str"), AnyValue.of(true), AnyValue.of(1), AnyValue.of(1.1)),
            "[str, true, 1, 1.1]"),
        // key value list from KeyAnyValue array
        arguments(
            AnyValue.of(
                KeyAnyValue.of("key1", AnyValue.of("val1")),
                KeyAnyValue.of("key2", AnyValue.of(2))),
            "[key1=val1, key2=2]"),
        // key value list from map
        arguments(
            AnyValue.of(
                new LinkedHashMap<String, AnyValue<?>>() {
                  {
                    put("key1", AnyValue.of("val1"));
                    put("key2", AnyValue.of(2));
                  }
                }),
            "[key1=val1, key2=2]"),
        // map of map
        arguments(
            AnyValue.of(
                Collections.singletonMap(
                    "child",
                    AnyValue.of(Collections.singletonMap("grandchild", AnyValue.of("str"))))),
            "[child=[grandchild=str]]"),
        // bytes
        arguments(AnyValue.of("hello world".getBytes(StandardCharsets.UTF_8)), "aGVsbG8gd29ybGQ="));
  }

  @Test
  void anyValueByteAsString() {
    // TODO: add more test cases
    String str = "hello world";
    String base64Encoded = AnyValue.of(str.getBytes(StandardCharsets.UTF_8)).asString();
    byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
    assertThat(new String(decodedBytes, StandardCharsets.UTF_8)).isEqualTo(str);
  }
}
