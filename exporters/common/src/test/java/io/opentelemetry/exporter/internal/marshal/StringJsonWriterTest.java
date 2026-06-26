/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class StringJsonWriterTest {

  @Test
  void emptyObject() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeEndObject();
                }))
        .isEqualTo("{}");
  }

  @Test
  void emptyArray() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartArray();
                  w.writeEndArray();
                }))
        .isEqualTo("[]");
  }

  @Test
  void singleFieldObject() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("key", "value");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"key\":\"value\"}");
  }

  @Test
  void multiFieldObject() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("a", "1");
                  w.writeNumberField("b", 2);
                  w.writeBooleanField("c", true);
                  w.writeEndObject();
                }))
        .isEqualTo("{\"a\":\"1\",\"b\":2,\"c\":true}");
  }

  @Test
  void multiElementArray() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartArray();
                  w.writeNumber(1);
                  w.writeNumber(2);
                  w.writeNumber(3);
                  w.writeEndArray();
                }))
        .isEqualTo("[1,2,3]");
  }

  @Test
  void nestedObjectsAndArrays() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeObjectFieldStart("nested");
                  w.writeArrayFieldStart("items");
                  w.writeNumber(1);
                  w.writeNumber(2);
                  w.writeEndArray();
                  w.writeEndObject();
                  w.writeEndObject();
                }))
        .isEqualTo("{\"nested\":{\"items\":[1,2]}}");
  }

  @Test
  void stringEscapingNamedEscapes() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("s", "a\"b\\c\bd\fe\nf\rg\th");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"s\":\"a\\\"b\\\\c\\bd\\fe\\nf\\rg\\th\"}");
  }

  @Test
  void stringEscapingControlChars() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("s", "\u0000\u0001\u001F");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"s\":\"\\u0000\\u0001\\u001F\"}");
  }

  @Test
  void forwardSlashNotEscaped() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("url", "http://example.com/path");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"url\":\"http://example.com/path\"}");
  }

  @Test
  void emptyString() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("s", "");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"s\":\"\"}");
  }

  @Test
  void nonAsciiUtf8() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("s", "é世界");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"s\":\"é世界\"}");
  }

  @Test
  void integers() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartArray();
                  w.writeNumber(0);
                  w.writeNumber(-1);
                  w.writeNumber(Integer.MAX_VALUE);
                  w.writeNumber(Integer.MIN_VALUE);
                  w.writeEndArray();
                }))
        .isEqualTo("[0,-1,2147483647,-2147483648]");
  }

  @Test
  void longs() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartArray();
                  w.writeNumber(0L);
                  w.writeNumber(-1L);
                  w.writeNumber(Long.MAX_VALUE);
                  w.writeNumber(Long.MIN_VALUE);
                  w.writeEndArray();
                }))
        .isEqualTo("[0,-1,9223372036854775807,-9223372036854775808]");
  }

  @Test
  void doubles() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartArray();
                  w.writeNumber(0.0);
                  w.writeNumber(-0.0);
                  w.writeNumber(1.0);
                  w.writeNumber(1.0E7);
                  w.writeNumber(1.0E-4);
                  w.writeEndArray();
                }))
        .isEqualTo("[0.0,-0.0,1.0,1.0E7,1.0E-4]");
  }

  @Test
  void base64EmptyArray() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeBinaryField("b", new byte[0]);
                  w.writeEndObject();
                }))
        .isEqualTo("{\"b\":\"\"}");
  }

  @Test
  void base64SingleByte() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeBinaryField("b", new byte[] {0x00});
                  w.writeEndObject();
                }))
        .isEqualTo("{\"b\":\"AA==\"}");
  }

  @Test
  void base64ThreeBytes() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeBinaryField("b", new byte[] {0x01, 0x02, 0x03});
                  w.writeEndObject();
                }))
        .isEqualTo("{\"b\":\"AQID\"}");
  }

  @Test
  void writeRawNoComma() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeRaw("\"raw\":true");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"raw\":true}");
  }

  private static String writeToString(WriteAction action) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (StringJsonWriter writer = new StringJsonWriter(sb)) {
      action.write(writer);
    }
    return sb.toString();
  }

  @FunctionalInterface
  interface WriteAction {
    void write(StringJsonWriter writer) throws IOException;
  }
}
