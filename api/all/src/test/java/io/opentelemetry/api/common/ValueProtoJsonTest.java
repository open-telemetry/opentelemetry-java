/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValueProtoJsonTest {

  @Test
  void valueString_basic() {
    assertThat(Value.of("hello").toProtoJson()).isEqualTo("\"hello\"");
  }

  @Test
  void valueString_empty() {
    assertThat(Value.of("").toProtoJson()).isEqualTo("\"\"");
  }

  @Test
  void valueString_withEscapes() {
    assertThat(Value.of("line1\nline2\ttab").toProtoJson()).isEqualTo("\"line1\\nline2\\ttab\"");
  }

  @Test
  void valueString_withQuotes() {
    assertThat(Value.of("say \"hello\"").toProtoJson()).isEqualTo("\"say \\\"hello\\\"\"");
  }

  @Test
  void valueString_withBackslash() {
    assertThat(Value.of("path\\to\\file").toProtoJson()).isEqualTo("\"path\\\\to\\\\file\"");
  }

  @Test
  void valueString_withControlCharacters() {
    assertThat(Value.of("\u0000\u0001\u001F").toProtoJson()).isEqualTo("\"\\u0000\\u0001\\u001f\"");
  }

  @Test
  void valueString_unicode() {
    assertThat(Value.of("Hello 世界 🌍").toProtoJson()).isEqualTo("\"Hello 世界 🌍\"");
  }

  @Test
  void valueBoolean_true() {
    assertThat(Value.of(true).toProtoJson()).isEqualTo("true");
  }

  @Test
  void valueBoolean_false() {
    assertThat(Value.of(false).toProtoJson()).isEqualTo("false");
  }

  @Test
  void valueLong_positive() {
    assertThat(Value.of(42L).toProtoJson()).isEqualTo("42");
  }

  @Test
  void valueLong_negative() {
    assertThat(Value.of(-123L).toProtoJson()).isEqualTo("-123");
  }

  @Test
  void valueLong_zero() {
    assertThat(Value.of(0L).toProtoJson()).isEqualTo("0");
  }

  @Test
  void valueLong_maxValue() {
    assertThat(Value.of(Long.MAX_VALUE).toProtoJson()).isEqualTo("9223372036854775807");
  }

  @Test
  void valueLong_minValue() {
    assertThat(Value.of(Long.MIN_VALUE).toProtoJson()).isEqualTo("-9223372036854775808");
  }

  @Test
  void valueDouble_regular() {
    assertThat(Value.of(3.14).toProtoJson()).isEqualTo("3.14");
  }

  @Test
  void valueDouble_negative() {
    assertThat(Value.of(-2.5).toProtoJson()).isEqualTo("-2.5");
  }

  @Test
  void valueDouble_zero() {
    assertThat(Value.of(0.0).toProtoJson()).isEqualTo("0.0");
  }

  @Test
  void valueDouble_negativeZero() {
    assertThat(Value.of(-0.0).toProtoJson()).isEqualTo("-0.0");
  }

  @Test
  void valueDouble_nan() {
    assertThat(Value.of(Double.NaN).toProtoJson()).isEqualTo("\"NaN\"");
  }

  @Test
  void valueDouble_positiveInfinity() {
    assertThat(Value.of(Double.POSITIVE_INFINITY).toProtoJson()).isEqualTo("\"Infinity\"");
  }

  @Test
  void valueDouble_negativeInfinity() {
    assertThat(Value.of(Double.NEGATIVE_INFINITY).toProtoJson()).isEqualTo("\"-Infinity\"");
  }

  @Test
  void valueDouble_scientificNotation() {
    assertThat(Value.of(1.23e10).toProtoJson()).isEqualTo("1.23E10");
  }

  @Test
  void valueDouble_verySmall() {
    assertThat(Value.of(1.23e-10).toProtoJson()).isEqualTo("1.23E-10");
  }

  @Test
  void valueBytes_empty() {
    assertThat(Value.of(new byte[] {}).toProtoJson()).isEqualTo("\"\"");
  }

  @Test
  void valueBytes_regular() {
    byte[] bytes = new byte[] {0, 1, 2, Byte.MAX_VALUE, Byte.MIN_VALUE};
    assertThat(Value.of(bytes).toProtoJson())
        .isEqualTo('"' + Base64.getEncoder().encodeToString(bytes) + '"');
  }

  @Test
  void valueEmpty() {
    assertThat(Value.empty().toProtoJson()).isEqualTo("null");
  }

  @Test
  @SuppressWarnings("ExplicitArrayForVarargs")
  void valueArray_empty() {
    assertThat(Value.of(new Value<?>[] {}).toProtoJson()).isEqualTo("[]");
  }

  @Test
  void valueArray_singleElement() {
    assertThat(Value.of(Value.of("test")).toProtoJson()).isEqualTo("[\"test\"]");
  }

  @Test
  void valueArray_multipleStrings() {
    assertThat(Value.of(Value.of("a"), Value.of("b"), Value.of("c")).toProtoJson())
        .isEqualTo("[\"a\",\"b\",\"c\"]");
  }

  @Test
  void valueArray_multipleNumbers() {
    assertThat(Value.of(Value.of(1L), Value.of(2L), Value.of(3L)).toProtoJson())
        .isEqualTo("[1,2,3]");
  }

  @Test
  void valueArray_mixedTypes() {
    assertThat(
            Value.of(
                    Value.of("string"),
                    Value.of(42L),
                    Value.of(3.14),
                    Value.of(true),
                    Value.of(false),
                    Value.empty())
                .toProtoJson())
        .isEqualTo("[\"string\",42,3.14,true,false,null]");
  }

  @Test
  void valueArray_nested() {
    assertThat(
            Value.of(
                    Value.of("outer"),
                    Value.of(Value.of("inner1"), Value.of("inner2")),
                    Value.of(42L))
                .toProtoJson())
        .isEqualTo("[\"outer\",[\"inner1\",\"inner2\"],42]");
  }

  @Test
  void valueArray_deeplyNested() {
    assertThat(
            Value.of(Value.of(Value.of(Value.of(Value.of(Value.of("deep"))))), Value.of("shallow"))
                .toProtoJson())
        .isEqualTo("[[[[[\"deep\"]]]],\"shallow\"]");
  }

  @Test
  @SuppressWarnings("ExplicitArrayForVarargs")
  void valueKeyValueList_empty() {
    assertThat(Value.of(new KeyValue[] {}).toProtoJson()).isEqualTo("{}");
  }

  @Test
  void valueKeyValueList_singleEntry() {
    assertThat(Value.of(KeyValue.of("key", Value.of("value"))).toProtoJson())
        .isEqualTo("{\"key\":\"value\"}");
  }

  @Test
  void valueKeyValueList_multipleEntries() {
    assertThat(
            Value.of(
                    KeyValue.of("name", Value.of("Alice")),
                    KeyValue.of("age", Value.of(30L)),
                    KeyValue.of("active", Value.of(true)))
                .toProtoJson())
        .isEqualTo("{\"name\":\"Alice\",\"age\":30,\"active\":true}");
  }

  @Test
  void valueKeyValueList_nestedMap() {
    assertThat(
            Value.of(
                    KeyValue.of("outer", Value.of("value")),
                    KeyValue.of(
                        "inner",
                        Value.of(
                            KeyValue.of("nested1", Value.of("a")),
                            KeyValue.of("nested2", Value.of("b")))))
                .toProtoJson())
        .isEqualTo("{\"outer\":\"value\",\"inner\":{\"nested1\":\"a\",\"nested2\":\"b\"}}");
  }

  @Test
  void valueKeyValueList_withArray() {
    assertThat(
            Value.of(
                    KeyValue.of("name", Value.of("test")),
                    KeyValue.of("items", Value.of(Value.of(1L), Value.of(2L), Value.of(3L))))
                .toProtoJson())
        .isEqualTo("{\"name\":\"test\",\"items\":[1,2,3]}");
  }

  @Test
  void valueKeyValueList_allTypes() {
    assertThat(
            Value.of(
                    KeyValue.of("string", Value.of("text")),
                    KeyValue.of("long", Value.of(42L)),
                    KeyValue.of("double", Value.of(3.14)),
                    KeyValue.of("bool", Value.of(true)),
                    KeyValue.of("empty", Value.empty()),
                    KeyValue.of("bytes", Value.of(new byte[] {1, 2})),
                    KeyValue.of("array", Value.of(Value.of("a"), Value.of("b"))))
                .toProtoJson())
        .isEqualTo(
            "{\"string\":\"text\",\"long\":42,\"double\":3.14,\"bool\":true,"
                + "\"empty\":null,\"bytes\":\"AQI=\",\"array\":[\"a\",\"b\"]}");
  }

  @Test
  void valueKeyValueList_fromMap() {
    Map<String, Value<?>> map = new LinkedHashMap<>();
    map.put("key1", Value.of("value1"));
    map.put("key2", Value.of(42L));
    assertThat(Value.of(map).toProtoJson()).isEqualTo("{\"key1\":\"value1\",\"key2\":42}");
  }

  @Test
  void valueKeyValueList_keyWithSpecialCharacters() {
    assertThat(
            Value.of(
                    KeyValue.of("key with spaces", Value.of("value1")),
                    KeyValue.of("key\"with\"quotes", Value.of("value2")),
                    KeyValue.of("key\nwith\nnewlines", Value.of("value3")))
                .toProtoJson())
        .isEqualTo(
            "{\"key with spaces\":\"value1\","
                + "\"key\\\"with\\\"quotes\":\"value2\","
                + "\"key\\nwith\\nnewlines\":\"value3\"}");
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

    assertThat(complexValue.toProtoJson())
        .isEqualTo(
            "{\"user\":\"Alice\","
                + "\"scores\":[95,87.5,92,\"NaN\",\"Infinity\"],"
                + "\"passed\":true,"
                + "\"metadata\":{\"timestamp\":1234567890,"
                + "\"tags\":[\"important\",\"reviewed\",\"final\"]}}");
  }

  @Test
  void edgeCase_emptyStringKey() {
    assertThat(Value.of(KeyValue.of("", Value.of("value"))).toProtoJson())
        .isEqualTo("{\"\":\"value\"}");
  }

  @Test
  void edgeCase_multipleEmptyValues() {
    assertThat(Value.of(Value.empty(), Value.empty(), Value.empty()).toProtoJson())
        .isEqualTo("[null,null,null]");
  }

  @Test
  void edgeCase_arrayOfMaps() {
    assertThat(
            Value.of(
                    Value.of(KeyValue.of("id", Value.of(1L)), KeyValue.of("name", Value.of("A"))),
                    Value.of(KeyValue.of("id", Value.of(2L)), KeyValue.of("name", Value.of("B"))),
                    Value.of(KeyValue.of("id", Value.of(3L)), KeyValue.of("name", Value.of("C"))))
                .toProtoJson())
        .isEqualTo(
            "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"},{\"id\":3,\"name\":\"C\"}]");
  }

  @Test
  @SuppressWarnings("ExplicitArrayForVarargs")
  void edgeCase_mapWithEmptyArray() {
    assertThat(
            Value.of(
                    KeyValue.of("data", Value.of("test")),
                    KeyValue.of("items", Value.of(new Value<?>[] {})))
                .toProtoJson())
        .isEqualTo("{\"data\":\"test\",\"items\":[]}");
  }

  @Test
  @SuppressWarnings("ExplicitArrayForVarargs")
  void edgeCase_mapWithEmptyMap() {
    assertThat(
            Value.of(
                    KeyValue.of("data", Value.of("test")),
                    KeyValue.of("metadata", Value.of(new KeyValue[] {})))
                .toProtoJson())
        .isEqualTo("{\"data\":\"test\",\"metadata\":{}}");
  }
}
