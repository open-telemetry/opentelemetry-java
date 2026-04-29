/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

@SuppressLogger(LoggerJsonWriter.class)
class LoggerJsonWriterTest {

  @RegisterExtension
  static final LogCapturer logs = LogCapturer.create().captureForType(LoggerJsonWriter.class);

  @Test
  void write_prettyPrintEnabled() throws IOException {
    Logger logger = mock(Logger.class);
    LoggerJsonWriter writer = new LoggerJsonWriter(logger, "type", true);

    writer.write(simpleObjectMarshaler());

    verify(logger)
        .log(Level.INFO, "{\n  \"key\" : \"value\"\n}".replaceAll("\n", System.lineSeparator()));
  }

  @Test
  void write_prettyPrintDisabled() throws IOException {
    Logger logger = mock(Logger.class);
    LoggerJsonWriter writer = new LoggerJsonWriter(logger, "type", false);

    writer.write(simpleObjectMarshaler());

    verify(logger).log(Level.INFO, "{\"key\":\"value\"}");
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
              return null;
            })
        .when(marshaler)
        .writeJsonToGenerator(any(JsonGenerator.class));
    return marshaler;
  }

  @Test
  void testToString() {
    LoggerJsonWriter writer = new LoggerJsonWriter(null, "type", false);
    assertThat(writer.toString()).isEqualTo("LoggerJsonWriter");
  }

  @Test
  void error() throws IOException {
    Marshaler marshaler = mock(Marshaler.class);
    Mockito.doThrow(new IOException("test"))
        .when(marshaler)
        .writeJsonToGenerator(any(JsonGenerator.class));

    Logger logger = Logger.getLogger(LoggerJsonWriter.class.getName());

    LoggerJsonWriter writer = new LoggerJsonWriter(logger, "type", false);
    writer.write(marshaler);

    logs.assertContains("Unable to write OTLP JSON type");
  }
}
