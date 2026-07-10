/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonBufferedEncoderTest {

  @FunctionalInterface
  private interface Body {
    void accept(JsonBufferedEncoder encoder) throws IOException;
  }

  @ParameterizedTest
  @MethodSource("cases")
  void writesExpectedJson(Body body, String expected) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JsonBufferedEncoder encoder = new JsonBufferedEncoder(bos);
    body.accept(encoder);
    encoder.close();
    assertThat(new String(bos.toByteArray(), StandardCharsets.UTF_8)).isEqualTo(expected);
  }

  // Writes a string whose UTF-8 encoding exceeds the encoder's 4096-byte internal buffer, forcing
  // at least one intermediate flush to the underlying stream before close().
  @Test
  void flushesWhenBufferFills() throws Exception {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 5000; i++) {
      sb.append('a');
    }
    String longValue = sb.toString();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JsonBufferedEncoder encoder = new JsonBufferedEncoder(bos);
    encoder.writeString(longValue);
    // Before close, the underlying stream must have received bytes from at least one mid-write
    // flush; otherwise everything would still be sitting in the 4096-byte buffer.
    assertThat(bos.size()).isGreaterThan(0);
    encoder.close();
    assertThat(new String(bos.toByteArray(), StandardCharsets.UTF_8))
        .isEqualTo("\"" + longValue + "\"");
  }

  // Nests more objects than the initial hasMembers array capacity (16), forcing the array to grow.
  @Test
  void growsWhenNestingExceedsInitialCapacity() throws Exception {
    int depth = 20;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JsonBufferedEncoder encoder = new JsonBufferedEncoder(bos);
    for (int i = 0; i < depth; i++) {
      encoder.writeStartObject();
      encoder.writeFieldName("k" + i);
    }
    encoder.writeString("v");
    for (int i = 0; i < depth; i++) {
      encoder.writeEndObject();
    }
    encoder.close();

    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      expected.append("{\"k").append(i).append("\":");
    }
    expected.append("\"v\"");
    for (int i = 0; i < depth; i++) {
      expected.append('}');
    }
    assertThat(new String(bos.toByteArray(), StandardCharsets.UTF_8))
        .isEqualTo(expected.toString());
  }

  // Unpaired surrogates cannot be encoded as UTF-8; the encoder emits '?' in their place so the
  // output remains valid UTF-8.
  @Test
  void invalidSurrogatesEmitReplacement() throws Exception {
    // Lone low surrogate (never preceded by a high surrogate).
    assertString(String.valueOf((char) 0xDC00), "\"?\"");
    // High surrogate at end of string (no following char).
    assertString(String.valueOf((char) 0xD800), "\"?\"");
    // High surrogate followed by a non-low-surrogate BMP character (U+00E9 e-acute); the high
    // surrogate is replaced and the following char is emitted normally.
    assertString("" + (char) 0xD800 + (char) 0x00E9, "\"?" + (char) 0xE9 + "\"");
  }

  @Test
  void nestingBeyondMaxDepthThrows() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JsonBufferedEncoder encoder = new JsonBufferedEncoder(bos);
    // MAX_NESTING_DEPTH successive writeStartObject calls are allowed; the next one must throw.
    for (int i = 0; i < JsonBufferedEncoder.MAX_NESTING_DEPTH; i++) {
      encoder.writeStartObject();
      encoder.writeFieldName("k");
    }
    assertThatThrownBy(encoder::writeStartObject)
        .isInstanceOf(IOException.class)
        .hasMessageContaining("nesting depth");
  }

  // close() drains the buffer to the underlying stream; if that write throws, the exception is
  // wrapped so it can propagate out of a try-with-resources without self-suppression issues.
  @Test
  void closeWrapsIoException() throws Exception {
    IOException boom = new IOException("boom");
    OutputStream failing =
        new OutputStream() {
          @Override
          public void write(int b) throws IOException {
            throw boom;
          }

          @Override
          public void write(byte[] b, int off, int len) throws IOException {
            throw boom;
          }
        };
    JsonBufferedEncoder encoder = new JsonBufferedEncoder(failing);
    // Write a single byte so there is buffered output for close() to attempt to flush.
    encoder.writeString("x");
    assertThatThrownBy(encoder::close).isInstanceOf(IOException.class).hasCause(boom);
  }

  private static void assertString(String input, String expected) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JsonBufferedEncoder encoder = new JsonBufferedEncoder(bos);
    encoder.writeString(input);
    encoder.close();
    assertThat(new String(bos.toByteArray(), StandardCharsets.UTF_8)).isEqualTo(expected);
  }

  static Stream<Arguments> cases() {
    // Built with concatenation to keep the source ASCII: quote, backslash, slash, control-char
    // shortcuts, NUL, U+001F, 'a', and the 2-/3-/4-byte UTF-8 chars U+00E9 (e-acute), U+4E16 (CJK),
    // and U+1F600 (grinning face). Forward slash is not escaped, control characters use lowercase
    // \\u00xx, and multi-byte UTF-8 passes through verbatim.
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
            + (char) 0x4e16
            + new String(Character.toChars(0x1f600));
    String escapeExpected =
        "\"\\\"\\\\/\\b\\f\\n\\r\\t\\u0000\\u001fa"
            + (char) 0xe9
            + (char) 0x4e16
            + new String(Character.toChars(0x1f600))
            + "\"";

    // 'a', TAB, then U+00E9 (e-acute) as its two UTF-8 bytes 0xC3 0xA9.
    byte[] utf8 = {'a', '\t', (byte) 0xc3, (byte) 0xa9};
    String utf8Expected = "\"a\\t" + (char) 0xe9 + "\"";

    return Stream.of(
        Arguments.argumentSet(
            "empty object",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeEndObject();
                },
            "{}"),
        Arguments.argumentSet(
            "object inserts separators between fields",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeStringField("a", "b");
                  encoder.writeNumberField("n", 1);
                  encoder.writeBooleanField("t", true);
                  encoder.writeBooleanField("f", false);
                  encoder.writeEndObject();
                },
            "{\"a\":\"b\",\"n\":1,\"t\":true,\"f\":false}"),
        Arguments.argumentSet(
            "nested objects and arrays",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeObjectFieldStart("o");
                  encoder.writeStringField("k", "v");
                  encoder.writeEndObject();
                  encoder.writeArrayFieldStart("arr");
                  encoder.writeString("x");
                  encoder.writeNumber(2.5);
                  encoder.writeEndArray();
                  encoder.writeEndObject();
                },
            "{\"o\":{\"k\":\"v\"},\"arr\":[\"x\",2.5]}"),
        Arguments.argumentSet(
            "string escaping", (Body) encoder -> encoder.writeString(escapeInput), escapeExpected),
        Arguments.argumentSet(
            "writeUtf8String escapes ascii and passes multi-byte through",
            (Body) encoder -> encoder.writeUtf8String(utf8),
            utf8Expected),
        Arguments.argumentSet(
            "integer boundaries",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeNumberField("min", Integer.MIN_VALUE);
                  encoder.writeNumberField("max", Integer.MAX_VALUE);
                  encoder.writeEndObject();
                },
            "{\"min\":-2147483648,\"max\":2147483647}"),
        Arguments.argumentSet(
            "exponent and signed-zero doubles",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeArrayFieldStart("d");
                  encoder.writeNumber(1.0e7);
                  encoder.writeNumber(1.0e-4);
                  encoder.writeNumber(-0.0);
                  encoder.writeNumber(0.0);
                  encoder.writeEndArray();
                  encoder.writeEndObject();
                },
            "{\"d\":[1.0E7,1.0E-4,-0.0,0.0]}"),
        Arguments.argumentSet(
            "empty base64",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeBinaryField("b", new byte[0]);
                  encoder.writeEndObject();
                },
            "{\"b\":\"\"}"),
        Arguments.argumentSet(
            "objects in array",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeArrayFieldStart("a");
                  encoder.writeStartObject();
                  encoder.writeNumberField("x", 1);
                  encoder.writeEndObject();
                  encoder.writeStartObject();
                  encoder.writeNumberField("y", 2);
                  encoder.writeEndObject();
                  encoder.writeEndArray();
                  encoder.writeEndObject();
                },
            "{\"a\":[{\"x\":1},{\"y\":2}]}"),
        Arguments.argumentSet(
            "non-finite doubles are quoted",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeArrayFieldStart("d");
                  encoder.writeNumber(Double.NaN);
                  encoder.writeNumber(Double.POSITIVE_INFINITY);
                  encoder.writeNumber(Double.NEGATIVE_INFINITY);
                  encoder.writeNumber(1.5);
                  encoder.writeEndArray();
                  encoder.writeEndObject();
                },
            "{\"d\":[\"NaN\",\"Infinity\",\"-Infinity\",1.5]}"),
        Arguments.argumentSet(
            "binary field is base64",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeBinaryField("b", new byte[] {1, 2, 3});
                  encoder.writeEndObject();
                },
            "{\"b\":\"AQID\"}"),
        // Mirrors MarshalerUtil#preserializeJsonFields: pre-serialized fields written raw into an
        // otherwise empty object, without disturbing separator state.
        Arguments.argumentSet(
            "writeRaw embeds verbatim",
            (Body)
                encoder -> {
                  encoder.writeStartObject();
                  encoder.writeObjectFieldStart("resource");
                  encoder.writeRaw("\"key\":\"val\"");
                  encoder.writeEndObject();
                  encoder.writeEndObject();
                },
            "{\"resource\":{\"key\":\"val\"}}"));
  }
}
