/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class StreamingJsonWriterTest {

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
  void mixedNesting() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeArrayFieldStart("a");
                  w.writeStartObject();
                  w.writeNumberField("b", 1);
                  w.writeEndObject();
                  w.writeStartObject();
                  w.writeArrayFieldStart("c");
                  w.writeNumber(2);
                  w.writeNumber(3);
                  w.writeEndArray();
                  w.writeEndObject();
                  w.writeEndArray();
                  w.writeEndObject();
                }))
        .isEqualTo("{\"a\":[{\"b\":1},{\"c\":[2,3]}]}");
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
    String result =
        writeToString(
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "é世界");
              w.writeEndObject();
            });
    assertThat(result).isEqualTo("{\"s\":\"é世界\"}");
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
  void base64WithPlusAndSlash() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeBinaryField("b", new byte[] {(byte) 0xFB, (byte) 0xFF});
                  w.writeEndObject();
                }))
        .isEqualTo("{\"b\":\"+/8=\"}");
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

  @Test
  void writeRawAfterFieldNoExtraComma() throws IOException {
    assertThat(
            writeToString(
                w -> {
                  w.writeStartObject();
                  w.writeStringField("a", "1");
                  w.writeRaw(",\"raw\":true");
                  w.writeEndObject();
                }))
        .isEqualTo("{\"a\":\"1\",\"raw\":true}");
  }

  @Test
  void closeFlushesBuffer() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamingJsonWriter writer = new StreamingJsonWriter(baos);
    writer.writeStartObject();
    writer.writeStringField("key", "value");
    writer.writeEndObject();
    assertThat(baos.size()).isEqualTo(0); // buffered, not yet flushed
    writer.close();
    assertThat(new String(baos.toByteArray(), StandardCharsets.UTF_8))
        .isEqualTo("{\"key\":\"value\"}");
  }

  @Test
  void closeDoesNotCloseUnderlyingStream() throws IOException {
    boolean[] closed = {false};
    ByteArrayOutputStream baos =
        new ByteArrayOutputStream() {
          @Override
          public void close() throws IOException {
            closed[0] = true;
            super.close();
          }
        };
    StreamingJsonWriter writer = new StreamingJsonWriter(baos);
    writer.writeStartObject();
    writer.writeEndObject();
    writer.close();
    assertThat(closed[0]).isFalse();
  }

  @Test
  void consistencyWithStringJsonWriter() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (StreamingJsonWriter sw = new StreamingJsonWriter(baos)) {
      writeComplexDocument(sw);
    }
    String fromStream = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    StringBuilder sb = new StringBuilder();
    try (StringJsonWriter sjw = new StringJsonWriter(sb)) {
      writeComplexDocument(sjw);
    }
    String fromString = sb.toString();

    assertThat(fromStream).isEqualTo(fromString);
  }

  private static void writeComplexDocument(JsonWriterTarget w) throws IOException {
    w.writeStartObject();
    w.writeStringField("name", "test\nvalue");
    w.writeNumberField("int", 42);
    w.writeNumberField("long", 123456789012345L);
    w.writeNumberField("double", 3.14);
    w.writeBooleanField("flag", true);
    w.writeBinaryField("data", new byte[] {1, 2, 3});
    w.writeArrayFieldStart("items");
    w.writeString("a");
    w.writeNumber(1);
    w.writeBoolean(false);
    w.writeEndArray();
    w.writeObjectFieldStart("nested");
    w.writeStringField("key", "val");
    w.writeEndObject();
    w.writeEndObject();
  }

  private static String writeToString(WriteAction action) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (StreamingJsonWriter writer = new StreamingJsonWriter(baos)) {
      action.write(writer);
    }
    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
  }

  @FunctionalInterface
  interface WriteAction {
    void write(StreamingJsonWriter writer) throws IOException;
  }
}
