/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.json.jackson3.internal;

import io.opentelemetry.sdk.common.export.JsonProvider;
import io.opentelemetry.sdk.common.export.JsonStringWriter;
import io.opentelemetry.sdk.common.export.JsonWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.json.JsonFactory;

/**
 * {@link JsonProvider} implementation backed by Jackson 3.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("CheckedExceptionNotThrown")
public final class Jackson3JsonProvider implements JsonProvider {

  private static final JsonFactory JSON_FACTORY = JsonFactory.builder().build();

  private static final JsonFactory JSON_FACTORY_NO_AUTO_CLOSE =
      JsonFactory.builder().disable(StreamWriteFeature.AUTO_CLOSE_TARGET).build();

  @Override
  public JsonWriter createJsonWriter(OutputStream output) throws IOException {
    return new Jackson3JsonWriter(JSON_FACTORY.createGenerator(ObjectWriteContext.empty(), output));
  }

  @Override
  public JsonWriter createJsonWriter(OutputStream output, boolean autoCloseTarget)
      throws IOException {
    JsonFactory factory = autoCloseTarget ? JSON_FACTORY : JSON_FACTORY_NO_AUTO_CLOSE;
    return new Jackson3JsonWriter(factory.createGenerator(ObjectWriteContext.empty(), output));
  }

  @Override
  public JsonStringWriter createJsonStringWriter() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer =
        new Jackson3JsonWriter(
            JSON_FACTORY.createGenerator(ObjectWriteContext.empty(), stringWriter));
    return new Jackson3StringWriter(writer, stringWriter);
  }

  private static final class Jackson3StringWriter implements JsonStringWriter {

    private final JsonWriter writer;
    private final StringWriter stringWriter;

    Jackson3StringWriter(JsonWriter writer, StringWriter stringWriter) {
      this.writer = writer;
      this.stringWriter = stringWriter;
    }

    @Override
    public JsonWriter writer() {
      return writer;
    }

    @Override
    public String getAndClear() {
      String result = stringWriter.toString();
      stringWriter.getBuffer().setLength(0);
      return result;
    }
  }
}
