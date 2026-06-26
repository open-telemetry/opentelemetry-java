/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.json.jackson2.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.opentelemetry.sdk.common.export.JsonProvider;
import io.opentelemetry.sdk.common.export.JsonStringWriter;
import io.opentelemetry.sdk.common.export.JsonWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link JsonProvider} implementation backed by Jackson 2.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class Jackson2JsonProvider implements JsonProvider {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  @Override
  public JsonWriter createJsonWriter(OutputStream output) throws IOException {
    return new Jackson2JsonWriter(JSON_FACTORY.createGenerator(output));
  }

  @Override
  public JsonWriter createJsonWriter(OutputStream output, boolean autoCloseTarget)
      throws IOException {
    JsonGenerator generator = JSON_FACTORY.createGenerator(output);
    if (!autoCloseTarget) {
      generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }
    return new Jackson2JsonWriter(generator);
  }

  @Override
  public JsonStringWriter createJsonStringWriter() throws IOException {
    @SuppressWarnings("deprecation")
    SegmentedStringWriter stringWriter =
        new SegmentedStringWriter(JSON_FACTORY._getBufferRecycler());
    JsonWriter writer = new Jackson2JsonWriter(JSON_FACTORY.createGenerator(stringWriter));
    return new Jackson2StringWriter(writer, stringWriter);
  }

  private static final class Jackson2StringWriter implements JsonStringWriter {

    private final JsonWriter writer;
    private final SegmentedStringWriter stringWriter;

    Jackson2StringWriter(JsonWriter writer, SegmentedStringWriter stringWriter) {
      this.writer = writer;
      this.stringWriter = stringWriter;
    }

    @Override
    public JsonWriter writer() {
      return writer;
    }

    @Override
    public String getAndClear() throws IOException {
      return stringWriter.getAndClear();
    }
  }
}
