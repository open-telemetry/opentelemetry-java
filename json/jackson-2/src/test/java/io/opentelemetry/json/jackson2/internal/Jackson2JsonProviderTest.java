/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.json.jackson2.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.export.JsonProvider;
import io.opentelemetry.sdk.common.export.JsonStringWriter;
import io.opentelemetry.sdk.common.export.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class Jackson2JsonProviderTest {

  private final Jackson2JsonProvider provider = new Jackson2JsonProvider();

  @Test
  void serviceLoaderLoads() {
    assertThat(ServiceLoader.load(JsonProvider.class))
        .anyMatch(p -> p instanceof Jackson2JsonProvider);
  }

  @Test
  void writeToOutputStream() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (JsonWriter writer = provider.createJsonWriter(baos)) {
      writeTestObject(writer);
    }
    assertThat(new String(baos.toByteArray(), StandardCharsets.UTF_8))
        .isEqualTo("{\"name\":\"test\",\"value\":42,\"active\":true}");
  }

  @Test
  void writeToString() throws IOException {
    JsonStringWriter stringWriter = provider.createJsonStringWriter();
    try (JsonWriter writer = stringWriter.writer()) {
      writeTestObject(writer);
    }
    assertThat(stringWriter.getAndClear())
        .isEqualTo("{\"name\":\"test\",\"value\":42,\"active\":true}");
  }

  @Test
  void autoCloseTargetTrue() throws IOException {
    TestOutputStream stream = new TestOutputStream();
    try (JsonWriter writer = provider.createJsonWriter(stream, true)) {
      writer.writeStartObject();
      writer.writeEndObject();
    }
    assertThat(stream.closed).isTrue();
  }

  @Test
  void autoCloseTargetFalse() throws IOException {
    TestOutputStream stream = new TestOutputStream();
    try (JsonWriter writer = provider.createJsonWriter(stream, false)) {
      writer.writeStartObject();
      writer.writeEndObject();
    }
    assertThat(stream.closed).isFalse();
  }

  @Test
  void writeNestedStructure() throws IOException {
    JsonStringWriter stringWriter = provider.createJsonStringWriter();
    try (JsonWriter writer = stringWriter.writer()) {
      writer.writeStartObject();
      writer.writeArrayFieldStart("items");
      writer.writeStartObject();
      writer.writeNumberField("id", 1);
      writer.writeEndObject();
      writer.writeEndArray();
      writer.writeObjectFieldStart("nested");
      writer.writeNumberField("pi", 3.14);
      writer.writeEndObject();
      writer.writeBinaryField("data", new byte[] {1, 2, 3});
      writer.writeEndObject();
    }
    String json = stringWriter.getAndClear();
    assertThat(json).contains("\"items\":[{\"id\":1}]");
    assertThat(json).contains("\"nested\":{\"pi\":3.14}");
    assertThat(json).contains("\"data\":");
  }

  private static void writeTestObject(JsonWriter writer) throws IOException {
    writer.writeStartObject();
    writer.writeStringField("name", "test");
    writer.writeNumberField("value", 42);
    writer.writeBooleanField("active", true);
    writer.writeEndObject();
  }

  private static class TestOutputStream extends ByteArrayOutputStream {
    boolean closed;

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }
}
