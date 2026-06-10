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

class ValueAsStringTest {

  @ParameterizedTest
  @MethodSource("stringValueProvider")
  void valueString(String input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> stringValueProvider() {
    return Stream.of(
        Arguments.argumentSet("hello", "hello", "hello"),
        Arguments.argumentSet("empty string", "", ""),
        Arguments.argumentSet("newline and tab", "line1\nline2\ttab", "line1\nline2\ttab"),
        Arguments.argumentSet("double quotes", "say \"hello\"", "say \"hello\""),
        Arguments.argumentSet("backslash path", "path\\to\\file", "path\\to\\file"),
        Arguments.argumentSet("control chars", "\u0000\u0001\u001F", "\u0000\u0001\u001F"),
        Arguments.argumentSet("unicode", "Hello 世界 🌍", "Hello 世界 🌍"));
  }

  @ParameterizedTest
  @MethodSource("booleanValueProvider")
  void valueBoolean(boolean input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> booleanValueProvider() {
    return Stream.of(
        Arguments.argumentSet("true", true, "true"),
        Arguments.argumentSet("false", false, "false"));
  }

  @ParameterizedTest
  @MethodSource("longValueProvider")
  void valueLong(long input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> longValueProvider() {
    return Stream.of(
        Arguments.argumentSet("42L", 42L, "42"),
        Arguments.argumentSet("-123L", -123L, "-123"),
        Arguments.argumentSet("0L", 0L, "0"),
        Arguments.argumentSet("Long.MAX_VALUE", Long.MAX_VALUE, "9223372036854775807"),
        Arguments.argumentSet("Long.MIN_VALUE", Long.MIN_VALUE, "-9223372036854775808"));
  }

  @ParameterizedTest
  @MethodSource("doubleValueProvider")
  void valueDouble(double input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> doubleValueProvider() {
    return Stream.of(
        Arguments.argumentSet("3.14", 3.14, "3.14"),
        Arguments.argumentSet("-2.5", -2.5, "-2.5"),
        Arguments.argumentSet("0.0", 0.0, "0.0"),
        Arguments.argumentSet("-0.0", -0.0, "-0.0"),
        Arguments.argumentSet("NaN", Double.NaN, "NaN"),
        Arguments.argumentSet("Infinity", Double.POSITIVE_INFINITY, "Infinity"),
        Arguments.argumentSet("-Infinity", Double.NEGATIVE_INFINITY, "-Infinity"),
        Arguments.argumentSet("1.23e10", 1.23e10, "1.23E10"),
        Arguments.argumentSet("1.23e-10", 1.23e-10, "1.23E-10"));
  }

  @ParameterizedTest
  @MethodSource("bytesValueProvider")
  void valueBytes(byte[] input, String expectedJson) {
    assertThat(Value.of(input).asString()).isEqualTo(expectedJson);
  }

  private static Stream<Arguments> bytesValueProvider() {
    byte[] regularBytes = new byte[] {0, 1, 2, Byte.MAX_VALUE, Byte.MIN_VALUE};
    return Stream.of(
        Arguments.argumentSet("empty bytes", new byte[] {}, ""),
        Arguments.argumentSet(
            "regular bytes", regularBytes, Base64.getEncoder().encodeToString(regularBytes)));
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
        Arguments.argumentSet("empty array", Value.of(new Value<?>[] {}), "[]"),
        Arguments.argumentSet("single string", Value.of(Value.of("test")), "[\"test\"]"),
        Arguments.argumentSet(
            "string array",
            Value.of(Value.of("a"), Value.of("b"), Value.of("c")),
            "[\"a\",\"b\",\"c\"]"),
        Arguments.argumentSet(
            "long array", Value.of(Value.of(1L), Value.of(2L), Value.of(3L)), "[1,2,3]"),
        Arguments.argumentSet(
            "mixed types",
            Value.of(
                Value.of("string"),
                Value.of(42L),
                Value.of(3.14),
                Value.of(true),
                Value.of(false),
                Value.empty()),
            "[\"string\",42,3.14,true,false,null]"),
        Arguments.argumentSet(
            "nested array",
            Value.of(
                Value.of("outer"), Value.of(Value.of("inner1"), Value.of("inner2")), Value.of(42L)),
            "[\"outer\",[\"inner1\",\"inner2\"],42]"),
        Arguments.argumentSet(
            "deeply nested",
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
        Arguments.argumentSet("empty kvlist", Value.of(new KeyValue[] {}), "{}"),
        Arguments.argumentSet(
            "single kv", Value.of(KeyValue.of("key", Value.of("value"))), "{\"key\":\"value\"}"),
        Arguments.argumentSet(
            "kv with mixed types",
            Value.of(
                KeyValue.of("name", Value.of("Alice")),
                KeyValue.of("age", Value.of(30L)),
                KeyValue.of("active", Value.of(true))),
            "{\"name\":\"Alice\",\"age\":30,\"active\":true}"),
        Arguments.argumentSet(
            "nested kvlist",
            Value.of(
                KeyValue.of("outer", Value.of("value")),
                KeyValue.of(
                    "inner",
                    Value.of(
                        KeyValue.of("nested1", Value.of("a")),
                        KeyValue.of("nested2", Value.of("b"))))),
            "{\"outer\":\"value\",\"inner\":{\"nested1\":\"a\",\"nested2\":\"b\"}}"),
        Arguments.argumentSet(
            "kv with array value",
            Value.of(
                KeyValue.of("name", Value.of("test")),
                KeyValue.of("items", Value.of(Value.of(1L), Value.of(2L), Value.of(3L)))),
            "{\"name\":\"test\",\"items\":[1,2,3]}"),
        Arguments.argumentSet(
            "all value types",
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
        Arguments.argumentSet("map value", Value.of(map), "{\"key1\":\"value1\",\"key2\":42}"),
        Arguments.argumentSet(
            "keys with special chars",
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
                + "\"scores\":[95,87.5,92,\"NaN\",\"Infinity\"],"
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
        Arguments.argumentSet(
            "empty key", Value.of(KeyValue.of("", Value.of("value"))), "{\"\":\"value\"}"),
        Arguments.argumentSet(
            "null values",
            Value.of(Value.empty(), Value.empty(), Value.empty()),
            "[null,null,null]"),
        Arguments.argumentSet(
            "array of kvlists",
            Value.of(
                Value.of(KeyValue.of("id", Value.of(1L)), KeyValue.of("name", Value.of("A"))),
                Value.of(KeyValue.of("id", Value.of(2L)), KeyValue.of("name", Value.of("B"))),
                Value.of(KeyValue.of("id", Value.of(3L)), KeyValue.of("name", Value.of("C")))),
            "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"},{\"id\":3,\"name\":\"C\"}]"),
        Arguments.argumentSet(
            "kv with empty array",
            Value.of(
                KeyValue.of("data", Value.of("test")),
                KeyValue.of("items", Value.of(new Value<?>[] {}))),
            "{\"data\":\"test\",\"items\":[]}"),
        Arguments.argumentSet(
            "kv with empty kvlist",
            Value.of(
                KeyValue.of("data", Value.of("test")),
                KeyValue.of("metadata", Value.of(new KeyValue[] {}))),
            "{\"data\":\"test\",\"metadata\":{}}"));
  }
}
