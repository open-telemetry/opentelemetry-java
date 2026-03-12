/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonGenerator;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

@SuppressWarnings("SystemOut")
@SuppressLogger(StreamJsonWriter.class)
class StreamJsonWriterTest {

  @RegisterExtension
  static final LogCapturer logs = LogCapturer.create().captureForType(StreamJsonWriter.class);

  @TempDir Path tempDir;

  @Test
  void write_prettyPrintEnabled() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamJsonWriter writer = new StreamJsonWriter(baos, "type", true);

    writer.write(simpleObjectMarshaler());

    assertThat(
            new String(baos.toByteArray(), StandardCharsets.UTF_8)
                .replace("\n", System.lineSeparator()))
        .isEqualTo("{\n  \"key\" : \"value\"\n}\n");
  }

  @Test
  void write_prettyPrintDisabled() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamJsonWriter writer = new StreamJsonWriter(baos, "type", false);

    writer.write(simpleObjectMarshaler());

    assertThat(new String(baos.toByteArray(), StandardCharsets.UTF_8))
        .isEqualTo("{\"key\":\"value\"}\n");
  }

  private static Marshaler simpleObjectMarshaler() throws IOException {
    Marshaler marshaler = mock(Marshaler.class);
    doAnswer(
            invocation -> {
              JsonGenerator gen = invocation.getArgument(0);
              gen.writeStartObject();
              gen.writeFieldName("key");
              gen.writeString("value");
              gen.writeEndObject();
              gen.writeRaw("\n");
              return null;
            })
        .when(marshaler)
        .writeJsonWithNewline(any(JsonGenerator.class));
    return marshaler;
  }

  @Test
  @SuppressWarnings("SystemOut")
  void testToString() throws IOException {
    assertThat(
            new StreamJsonWriter(Files.newOutputStream(tempDir.resolve("foo")), "type", false)
                .toString())
        .startsWith("StreamJsonWriter{outputStream=")
        .contains("Channel");
    assertThat(new StreamJsonWriter(System.out, "type", false).toString())
        .isEqualTo("StreamJsonWriter{outputStream=stdout}");
    assertThat(new StreamJsonWriter(System.err, "type", false).toString())
        .isEqualTo("StreamJsonWriter{outputStream=stderr}");
  }

  @Test
  void errorWriting() throws IOException {
    Marshaler marshaler = mock(Marshaler.class);
    Mockito.doThrow(new IOException("test"))
        .when(marshaler)
        .writeJsonWithNewline(any(JsonGenerator.class));

    StreamJsonWriter writer = new StreamJsonWriter(System.out, "type", false);
    writer.write(marshaler);

    logs.assertContains("Unable to write OTLP JSON type");
  }

  @Test
  void errorFlushing() {
    OutputStream outputStream =
        new FilterOutputStream(System.out) {
          @Override
          public void flush() throws IOException {
            throw new IOException("No flush");
          }
        };

    StreamJsonWriter writer = new StreamJsonWriter(outputStream, "type", false);
    writer.flush();

    logs.assertContains("Failed to flush items");
  }
}
