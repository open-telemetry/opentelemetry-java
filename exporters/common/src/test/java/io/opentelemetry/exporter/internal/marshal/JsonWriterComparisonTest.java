/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class JsonWriterComparisonTest {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  static Stream<TestCase> testCases() {
    return Stream.of(
        // Structural
        testCase(
            "emptyObject",
            w -> {
              w.writeStartObject();
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeEndObject();
            }),
        testCase(
            "emptyArray",
            w -> {
              w.writeStartArray();
              w.writeEndArray();
            },
            g -> {
              g.writeStartArray();
              g.writeEndArray();
            }),
        testCase(
            "singleField",
            w -> {
              w.writeStartObject();
              w.writeStringField("a", "1");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("a", "1");
              g.writeEndObject();
            }),
        testCase(
            "multiField",
            w -> {
              w.writeStartObject();
              w.writeStringField("a", "1");
              w.writeNumberField("b", 2);
              w.writeBooleanField("c", true);
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("a", "1");
              g.writeNumberField("b", 2);
              g.writeBooleanField("c", true);
              g.writeEndObject();
            }),
        testCase(
            "nestedObjects",
            w -> {
              w.writeStartObject();
              w.writeObjectFieldStart("n");
              w.writeStringField("k", "v");
              w.writeEndObject();
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeObjectFieldStart("n");
              g.writeStringField("k", "v");
              g.writeEndObject();
              g.writeEndObject();
            }),
        testCase(
            "nestedArrays",
            w -> {
              w.writeStartObject();
              w.writeArrayFieldStart("a");
              w.writeNumber(1);
              w.writeNumber(2);
              w.writeEndArray();
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeArrayFieldStart("a");
              g.writeNumber(1);
              g.writeNumber(2);
              g.writeEndArray();
              g.writeEndObject();
            }),
        testCase(
            "objectInArray",
            w -> {
              w.writeStartArray();
              w.writeStartObject();
              w.writeNumberField("a", 1);
              w.writeEndObject();
              w.writeStartObject();
              w.writeNumberField("b", 2);
              w.writeEndObject();
              w.writeEndArray();
            },
            g -> {
              g.writeStartArray();
              g.writeStartObject();
              g.writeNumberField("a", 1);
              g.writeEndObject();
              g.writeStartObject();
              g.writeNumberField("b", 2);
              g.writeEndObject();
              g.writeEndArray();
            }),

        // String escaping
        testCase(
            "namedEscapes",
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "a\"b\\c\bd\fe\nf\rg\th");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("s", "a\"b\\c\bd\fe\nf\rg\th");
              g.writeEndObject();
            }),
        testCase(
            "controlChars",
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "\u0000\u0001\u001F");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("s", "\u0000\u0001\u001F");
              g.writeEndObject();
            }),
        testCase(
            "forwardSlash",
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "a/b");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("s", "a/b");
              g.writeEndObject();
            }),
        testCase(
            "emptyString",
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("s", "");
              g.writeEndObject();
            }),
        testCase(
            "unicode",
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "é世界");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("s", "é世界");
              g.writeEndObject();
            }),
        testCase(
            "mixedEscapes",
            w -> {
              w.writeStartObject();
              w.writeStringField("s", "he said \"hello\"\nline2\ttab");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeStringField("s", "he said \"hello\"\nline2\ttab");
              g.writeEndObject();
            }),

        // Numbers
        testCase(
            "integers",
            w -> {
              w.writeStartArray();
              w.writeNumber(0);
              w.writeNumber(-1);
              w.writeNumber(Integer.MAX_VALUE);
              w.writeNumber(Integer.MIN_VALUE);
              w.writeEndArray();
            },
            g -> {
              g.writeStartArray();
              g.writeNumber(0);
              g.writeNumber(-1);
              g.writeNumber(Integer.MAX_VALUE);
              g.writeNumber(Integer.MIN_VALUE);
              g.writeEndArray();
            }),
        testCase(
            "longs",
            w -> {
              w.writeStartArray();
              w.writeNumber(0L);
              w.writeNumber(-1L);
              w.writeNumber(Long.MAX_VALUE);
              w.writeNumber(Long.MIN_VALUE);
              w.writeEndArray();
            },
            g -> {
              g.writeStartArray();
              g.writeNumber(0L);
              g.writeNumber(-1L);
              g.writeNumber(Long.MAX_VALUE);
              g.writeNumber(Long.MIN_VALUE);
              g.writeEndArray();
            }),
        testCase(
            "doubles",
            w -> {
              w.writeStartArray();
              w.writeNumber(0.0);
              w.writeNumber(-0.0);
              w.writeNumber(1.0);
              w.writeNumber(1.0E7);
              w.writeNumber(1.0E-4);
              w.writeNumber(3.141592653589793);
              w.writeEndArray();
            },
            g -> {
              g.writeStartArray();
              g.writeNumber(0.0);
              g.writeNumber(-0.0);
              g.writeNumber(1.0);
              g.writeNumber(1.0E7);
              g.writeNumber(1.0E-4);
              g.writeNumber(3.141592653589793);
              g.writeEndArray();
            }),

        // Base64
        testCase(
            "base64Empty",
            w -> {
              w.writeStartObject();
              w.writeBinaryField("b", new byte[0]);
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeBinaryField("b", new byte[0]);
              g.writeEndObject();
            }),
        testCase(
            "base64SingleByte",
            w -> {
              w.writeStartObject();
              w.writeBinaryField("b", new byte[] {0x01, 0x02, 0x03});
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeBinaryField("b", new byte[] {0x01, 0x02, 0x03});
              g.writeEndObject();
            }),

        // Boolean
        testCase(
            "booleans",
            w -> {
              w.writeStartArray();
              w.writeBoolean(true);
              w.writeBoolean(false);
              w.writeEndArray();
            },
            g -> {
              g.writeStartArray();
              g.writeBoolean(true);
              g.writeBoolean(false);
              g.writeEndArray();
            }),

        // writeRaw
        testCase(
            "writeRaw",
            w -> {
              w.writeStartObject();
              w.writeRaw("\"raw\":true");
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeRaw("\"raw\":true");
              g.writeEndObject();
            }),

        // OTLP-like pattern: resource with attributes
        testCase(
            "otlpResourcePattern",
            w -> {
              w.writeStartObject();
              w.writeObjectFieldStart("resource");
              w.writeArrayFieldStart("attributes");
              w.writeStartObject();
              w.writeStringField("key", "service.name");
              w.writeObjectFieldStart("value");
              w.writeStringField("stringValue", "my-service");
              w.writeEndObject();
              w.writeEndObject();
              w.writeEndArray();
              w.writeEndObject();
              w.writeEndObject();
            },
            g -> {
              g.writeStartObject();
              g.writeObjectFieldStart("resource");
              g.writeArrayFieldStart("attributes");
              g.writeStartObject();
              g.writeStringField("key", "service.name");
              g.writeObjectFieldStart("value");
              g.writeStringField("stringValue", "my-service");
              g.writeEndObject();
              g.writeEndObject();
              g.writeEndArray();
              g.writeEndObject();
              g.writeEndObject();
            }));
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void streamingWriterMatchesJackson(TestCase testCase) throws IOException {
    ByteArrayOutputStream builtIn = new ByteArrayOutputStream();
    try (StreamingJsonWriter writer = new StreamingJsonWriter(builtIn)) {
      testCase.builtInWriter.write(writer);
    }

    ByteArrayOutputStream jackson = new ByteArrayOutputStream();
    try (JsonGenerator gen = JSON_FACTORY.createGenerator(jackson)) {
      testCase.jacksonWriter.write(gen);
    }

    assertThat(builtIn.toByteArray())
        .as(
            "Built-in output should match Jackson for case: %s\nBuilt-in: %s\nJackson: %s",
            testCase.name,
            new String(builtIn.toByteArray(), StandardCharsets.UTF_8),
            new String(jackson.toByteArray(), StandardCharsets.UTF_8))
        .isEqualTo(jackson.toByteArray());
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void stringWriterMatchesJackson(TestCase testCase) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (StringJsonWriter writer = new StringJsonWriter(sb)) {
      testCase.builtInWriter.write(writer);
    }

    ByteArrayOutputStream jackson = new ByteArrayOutputStream();
    try (JsonGenerator gen = JSON_FACTORY.createGenerator(jackson)) {
      testCase.jacksonWriter.write(gen);
    }

    assertThat(sb.toString())
        .as("StringJsonWriter output should match Jackson for case: %s", testCase.name)
        .isEqualTo(new String(jackson.toByteArray(), StandardCharsets.UTF_8));
  }

  private static TestCase testCase(
      String name, BuiltInWriteAction builtIn, JacksonWriteAction jackson) {
    return new TestCase(name, builtIn, jackson);
  }

  static class TestCase {
    final String name;
    final BuiltInWriteAction builtInWriter;
    final JacksonWriteAction jacksonWriter;

    TestCase(String name, BuiltInWriteAction builtIn, JacksonWriteAction jackson) {
      this.name = name;
      this.builtInWriter = builtIn;
      this.jacksonWriter = jackson;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @FunctionalInterface
  interface BuiltInWriteAction {
    void write(JsonWriterTarget writer) throws IOException;
  }

  @FunctionalInterface
  interface JacksonWriteAction {
    void write(JsonGenerator gen) throws IOException;
  }
}
