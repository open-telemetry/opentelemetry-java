/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import java.nio.charset.StandardCharsets;
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
        arguments(AnyValue.ofString("str"), "str"),
        arguments(AnyValue.ofBoolean(true), "true"),
        arguments(AnyValue.ofLong(1), "1"),
        arguments(AnyValue.ofDouble(1.1), "1.1"),
        // heterogeneous array
        arguments(
            AnyValue.ofArray(
                AnyValue.ofString("str"),
                AnyValue.ofBoolean(true),
                AnyValue.ofLong(1),
                AnyValue.ofDouble(1.1)),
            "[str, true, 1, 1.1]"),
        // key value list from KeyAnyValue array
        arguments(
            AnyValue.ofKeyAnyValueArray(
                KeyAnyValue.of("key1", AnyValue.ofString("val1")),
                KeyAnyValue.of("key2", AnyValue.ofLong(2))),
            "[key1=val1, key2=2]"),
        // key value list from map
        arguments(
            AnyValue.ofMap(
                new LinkedHashMap<String, AnyValue<?>>() {
                  {
                    put("key1", AnyValue.ofString("val1"));
                    put("key2", AnyValue.ofLong(2));
                  }
                }),
            "[key1=val1, key2=2]"),
        // map of map
        arguments(
            AnyValue.ofKeyAnyValueArray(
                KeyAnyValue.of(
                    "child",
                    AnyValue.ofKeyAnyValueArray(
                        KeyAnyValue.of("grandchild", AnyValue.ofString("str"))))),
            "[child=[grandchild=str]]"),
        // bytes
        arguments(
            AnyValue.ofBytes("hello world".getBytes(StandardCharsets.UTF_8)),
            "68656c6c6f20776f726c64"));
  }

  @Test
  void anyValueByteAsString() {
    // TODO: add more test cases
    String str = "hello world";
    String base16Encoded = AnyValue.ofBytes(str.getBytes(StandardCharsets.UTF_8)).asString();
    byte[] decodedBytes = OtelEncodingUtils.bytesFromBase16(base16Encoded, base16Encoded.length());
    assertThat(new String(decodedBytes, StandardCharsets.UTF_8)).isEqualTo(str);
  }

  // TODO: test equals, hashcode, getType
}
