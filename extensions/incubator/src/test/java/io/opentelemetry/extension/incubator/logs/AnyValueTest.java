/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnyValueTest {

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
        arguments(
            AnyValue.of("hello world".getBytes(StandardCharsets.UTF_8)), "68656c6c6f20776f726c64"));
  }

  @Test
  void anyValueByteAsString() {
    // TODO: add more test cases
    String str = "hello world";
    String base16Encoded = AnyValue.of(str.getBytes(StandardCharsets.UTF_8)).asString();
    byte[] decodedBytes = OtelEncodingUtils.bytesFromBase16(base16Encoded, base16Encoded.length());
    assertThat(new String(decodedBytes, StandardCharsets.UTF_8)).isEqualTo(str);
  }

  // TODO: test equals, hashcode, getType
}
