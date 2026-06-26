/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonWriterTest {

  @FunctionalInterface
  private interface Body {
    void accept(JsonWriter writer) throws IOException;
  }

  @ParameterizedTest
  @MethodSource("cases")
  void writesExpectedJson(Body body, String expected) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JsonWriter writer = new JsonWriter(bos);
    try {
      body.accept(writer);
      writer.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    assertThat(new String(bos.toByteArray(), StandardCharsets.UTF_8)).isEqualTo(expected);
  }

  static Stream<Arguments> cases() {
    // Built with concatenation to keep the source ASCII: quote, backslash, slash, control-char
    // shortcuts, NUL, U+001F, 'a', U+00E9 (e-acute), and U+1F600 (grinning face). Forward slash is
    // not escaped, control characters use lowercase \\u00xx, and multi-byte UTF-8 passes through.
    String escapeInput =
        "\""
            + '\\'
            + '/'
            + '\b'
            + '\f'
            + '\n'
            + '\r'
            + '\t'
            + (char) 0x00
            + (char) 0x1f
            + 'a'
            + (char) 0xe9
            + new String(Character.toChars(0x1f600));
    String escapeExpected =
        "\"\\\"\\\\/\\b\\f\\n\\r\\t\\u0000\\u001fa"
            + (char) 0xe9
            + new String(Character.toChars(0x1f600))
            + "\"";

    // 'a', TAB, then U+00E9 (e-acute) as its two UTF-8 bytes 0xC3 0xA9.
    byte[] utf8 = {'a', '\t', (byte) 0xc3, (byte) 0xa9};
    String utf8Expected = "\"a\\t" + (char) 0xe9 + "\"";

    return Stream.of(
        Arguments.argumentSet(
            "empty object",
            (Body)
                writer -> {
                  writer.writeStartObject();
                  writer.writeEndObject();
                },
            "{}"),
        Arguments.argumentSet(
            "object inserts separators between fields",
            (Body)
                writer -> {
                  writer.writeStartObject();
                  writer.writeStringField("a", "b");
                  writer.writeNumberField("n", 1);
                  writer.writeBooleanField("t", true);
                  writer.writeBooleanField("f", false);
                  writer.writeEndObject();
                },
            "{\"a\":\"b\",\"n\":1,\"t\":true,\"f\":false}"),
        Arguments.argumentSet(
            "nested objects and arrays",
            (Body)
                writer -> {
                  writer.writeStartObject();
                  writer.writeObjectFieldStart("o");
                  writer.writeStringField("k", "v");
                  writer.writeEndObject();
                  writer.writeArrayFieldStart("arr");
                  writer.writeString("x");
                  writer.writeNumber(2.5);
                  writer.writeEndArray();
                  writer.writeEndObject();
                },
            "{\"o\":{\"k\":\"v\"},\"arr\":[\"x\",2.5]}"),
        Arguments.argumentSet(
            "string escaping", (Body) writer -> writer.writeString(escapeInput), escapeExpected),
        Arguments.argumentSet(
            "writeUtf8String escapes ascii and passes multi-byte through",
            (Body) writer -> writer.writeUtf8String(utf8),
            utf8Expected),
        Arguments.argumentSet(
            "non-finite doubles are quoted",
            (Body)
                writer -> {
                  writer.writeStartObject();
                  writer.writeArrayFieldStart("d");
                  writer.writeNumber(Double.NaN);
                  writer.writeNumber(Double.POSITIVE_INFINITY);
                  writer.writeNumber(Double.NEGATIVE_INFINITY);
                  writer.writeNumber(1.5);
                  writer.writeEndArray();
                  writer.writeEndObject();
                },
            "{\"d\":[\"NaN\",\"Infinity\",\"-Infinity\",1.5]}"),
        Arguments.argumentSet(
            "binary field is base64",
            (Body)
                writer -> {
                  writer.writeStartObject();
                  writer.writeBinaryField("b", new byte[] {1, 2, 3});
                  writer.writeEndObject();
                },
            "{\"b\":\"AQID\"}"),
        // Mirrors MarshalerUtil#preserializeJsonFields: pre-serialized fields written raw into an
        // otherwise empty object, without disturbing separator state.
        Arguments.argumentSet(
            "writeRaw embeds verbatim",
            (Body)
                writer -> {
                  writer.writeStartObject();
                  writer.writeObjectFieldStart("resource");
                  writer.writeRaw("\"key\":\"val\"");
                  writer.writeEndObject();
                  writer.writeEndObject();
                },
            "{\"resource\":{\"key\":\"val\"}}"));
  }
}
