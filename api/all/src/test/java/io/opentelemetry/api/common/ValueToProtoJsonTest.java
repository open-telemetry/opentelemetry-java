/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValueToProtoJsonTest {

  @ParameterizedTest
  @MethodSource("stringValueProvider")
  void valueString(String input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> stringValueProvider() {
    return Stream.of(
        Arguments.of("hello", "hello"),
        Arguments.of("", ""),
        Arguments.of("line1\nline2\ttab", "line1\nline2\ttab"),
        Arguments.of("say \"hello\"", "say \"hello\""),
        Arguments.of("path\\to\\file", "path\\to\\file"),
        Arguments.of("\u0000\u0001\u001F", "\u0000\u0001\u001F"),
        Arguments.of("Hello 世界 🌍", "Hello 世界 🌍"));
  }

  @ParameterizedTest
  @MethodSource("booleanValueProvider")
  void valueBoolean(boolean input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> booleanValueProvider() {
    return Stream.of(Arguments.of(true, "true"), Arguments.of(false, "false"));
  }

  @ParameterizedTest
  @MethodSource("longValueProvider")
  void valueLong(long input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> longValueProvider() {
    return Stream.of(
        Arguments.of(42L, "42"),
        Arguments.of(-123L, "-123"),
        Arguments.of(0L, "0"),
        Arguments.of(Long.MAX_VALUE, "9223372036854775807"),
        Arguments.of(Long.MIN_VALUE, "-9223372036854775808"));
  }

  @ParameterizedTest
  @MethodSource("doubleValueProvider")
  void valueDouble(double input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> doubleValueProvider() {
    return Stream.of(
        Arguments.of(3.14, "3.14"),
        Arguments.of(-2.5, "-2.5"),
        Arguments.of(0.0, "0.0"),
        Arguments.of(-0.0, "-0.0"),
        Arguments.of(Double.NaN, "NaN"),
        Arguments.of(Double.POSITIVE_INFINITY, "Infinity"),
        Arguments.of(Double.NEGATIVE_INFINITY, "-Infinity"),
        Arguments.of(1.23e10, "1.23E10"),
        Arguments.of(1.23e-10, "1.23E-10"));
  }

  @ParameterizedTest
  @MethodSource("bytesValueProvider")
  void valueBytes(byte[] input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> bytesValueProvider() {
    byte[] regularBytes = new byte[] {0, 1, 2, Byte.MAX_VALUE, Byte.MIN_VALUE};
    return Stream.of(
        Arguments.of(new byte[] {}, ""),
        Arguments.of(regularBytes, Base64.getEncoder().encodeToString(regularBytes)));
  }

  @Test
  void valueEmpty() {
    assertThat(Value.empty().asString()).isEqualTo("");
  }

  @ParameterizedTest
  @MethodSource("arrayValueProvider")
  @SuppressWarnings("ExplicitArrayForVarargs")
  void valueArray(Value<?> input, String expectedJson) {
    assertThat(input.asString()).isEqualTo(expectedJson);
  }

  @SuppressWarnings("ExplicitArrayForVarargs")
  private static Stream<Arguments> arrayValueProvider() {
    return Stream.of(
        Arguments.of(Value.of(new Value<?>[] {}), "[]"),
        Arguments.of(Value.of(Value.of("test")), "[\"test\"]"),
        Arguments.of(Value.of(Value.of("a"), Value.of("b"), Value.of("c")), "[\"a\",\"b\",\"c\"]"),
        Arguments.of(Value.of(Value.of(1L), Value.of(2L), Value.of(3L)), "[1,2,3]"),
        Arguments.of(
            Value.of(
                Value.of("string"),
                Value.of(42L),
                Value.of(3.14),
                Value.of(true),
                Value.of(false),
                Value.empty()),
            "[\"string\",42,3.14,true,false,null]"),
        Arguments.of(
            Value.of(
                Value.of("outer"), Value.of(Value.of("inner1"), Value.of("inner2")), Value.of(42L)),
            "[\"outer\",[\"inner1\",\"inner2\"],42]"),
        Arguments.of(
            Value.of(Value.of(Value.of(Value.of(Value.of(Value.of("deep"))))), Value.of("shallow")),
            "[[[[[\"deep\"]]]],\"shallow\"]"));
  }

  @ParameterizedTest
  @MethodSource("keyValueListValueProvider")
  @SuppressWarnings("ExplicitArrayForVarargs")
  void valueKeyValueList(Value<?> input, String expectedJson) {
    assertThat(input.asString()).isEqualTo(expectedJson);
  }

  @SuppressWarnings("ExplicitArrayForVarargs")
  private static Stream<Arguments> keyValueListValueProvider() {
    Map<String, Value<?>> map = new LinkedHashMap<>();
    map.put("key1", Value.of("value1"));
    map.put("key2", Value.of(42L));

    return Stream.of(
        Arguments.of(Value.of(new KeyValue[] {}), "{}"),
        Arguments.of(Value.of(KeyValue.of("key", Value.of("value"))), "{\"key\":\"value\"}"),
        Arguments.of(
            Value.of(
                KeyValue.of("name", Value.of("Alice")),
                KeyValue.of("age", Value.of(30L)),
                KeyValue.of("active", Value.of(true))),
            "{\"name\":\"Alice\",\"age\":30,\"active\":true}"),
        Arguments.of(
            Value.of(
                KeyValue.of("outer", Value.of("value")),
                KeyValue.of(
                    "inner",
                    Value.of(
                        KeyValue.of("nested1", Value.of("a")),
                        KeyValue.of("nested2", Value.of("b"))))),
            "{\"outer\":\"value\",\"inner\":{\"nested1\":\"a\",\"nested2\":\"b\"}}"),
        Arguments.of(
            Value.of(
                KeyValue.of("name", Value.of("test")),
                KeyValue.of("items", Value.of(Value.of(1L), Value.of(2L), Value.of(3L)))),
            "{\"name\":\"test\",\"items\":[1,2,3]}"),
        Arguments.of(
            Value.of(
                KeyValue.of("string", Value.of("text")),
                KeyValue.of("long", Value.of(42L)),
                KeyValue.of("double", Value.of(3.14)),
                KeyValue.of("bool", Value.of(true)),
                KeyValue.of("empty", Value.empty()),
                KeyValue.of("bytes", Value.of(new byte[] {1, 2})),
                KeyValue.of("array", Value.of(Value.of("a"), Value.of("b")))),
            "{\"string\":\"text\",\"long\":42,\"double\":3.14,\"bool\":true,"
                + "\"empty\":null,\"bytes\":\"AQI=\",\"array\":[\"a\",\"b\"]}"),
        Arguments.of(Value.of(map), "{\"key1\":\"value1\",\"key2\":42}"),
        Arguments.of(
            Value.of(
                KeyValue.of("key with spaces", Value.of("value1")),
                KeyValue.of("key\"with\"quotes", Value.of("value2")),
                KeyValue.of("key\nwith\nnewlines", Value.of("value3"))),
            "{\"key with spaces\":\"value1\","
                + "\"key\\\"with\\\"quotes\":\"value2\","
                + "\"key\\nwith\\nnewlines\":\"value3\"}"));
  }

  @Test
  void complexNestedStructure() {
    Value<?> complexValue =
        Value.of(
            KeyValue.of("user", Value.of("Alice")),
            KeyValue.of(
                "scores",
                Value.of(
                    Value.of(95L),
                    Value.of(87.5),
                    Value.of(92L),
                    Value.of(Double.NaN),
                    Value.of(Double.POSITIVE_INFINITY))),
            KeyValue.of("passed", Value.of(true)),
            KeyValue.of(
                "metadata",
                Value.of(
                    KeyValue.of("timestamp", Value.of(1234567890L)),
                    KeyValue.of(
                        "tags",
                        Value.of(
                            Value.of("important"), Value.of("reviewed"), Value.of("final"))))));

    assertThat(complexValue.asString())
        .isEqualTo(
            "{\"user\":\"Alice\","
                + "\"scores\":[95,87.5,92,NaN,Infinity],"
                + "\"passed\":true,"
                + "\"metadata\":{\"timestamp\":1234567890,"
                + "\"tags\":[\"important\",\"reviewed\",\"final\"]}}");
  }

  @ParameterizedTest
  @MethodSource("edgeCaseProvider")
  @SuppressWarnings("ExplicitArrayForVarargs")
  void edgeCases(Value<?> input, String expectedJson) {
    assertThat(input.asString()).isEqualTo(expectedJson);
  }

  @SuppressWarnings("ExplicitArrayForVarargs")
  private static Stream<Arguments> edgeCaseProvider() {
    return Stream.of(
        Arguments.of(Value.of(KeyValue.of("", Value.of("value"))), "{\"\":\"value\"}"),
        Arguments.of(Value.of(Value.empty(), Value.empty(), Value.empty()), "[null,null,null]"),
        Arguments.of(
            Value.of(
                Value.of(KeyValue.of("id", Value.of(1L)), KeyValue.of("name", Value.of("A"))),
                Value.of(KeyValue.of("id", Value.of(2L)), KeyValue.of("name", Value.of("B"))),
                Value.of(KeyValue.of("id", Value.of(3L)), KeyValue.of("name", Value.of("C")))),
            "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"},{\"id\":3,\"name\":\"C\"}]"),
        Arguments.of(
            Value.of(
                KeyValue.of("data", Value.of("test")),
                KeyValue.of("items", Value.of(new Value<?>[] {}))),
            "{\"data\":\"test\",\"items\":[]}"),
        Arguments.of(
            Value.of(
                KeyValue.of("data", Value.of("test")),
                KeyValue.of("metadata", Value.of(new KeyValue[] {}))),
            "{\"data\":\"test\",\"metadata\":{}}"));
  }
}
