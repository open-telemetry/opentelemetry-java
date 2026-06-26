/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.JsonProviderUtil;
import io.opentelemetry.sdk.common.export.JsonProvider;
import io.opentelemetry.sdk.common.export.JsonWriter;
import io.opentelemetry.sdk.common.export.MessageWriter;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

/**
 * Marshaler from an SDK structure to protobuf wire format.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class Marshaler {

  private static final Object lock = new Object();
  @Nullable private static volatile JsonProvider jsonProvider;

  /** Marshals into the {@link OutputStream} in proto binary format. */
  public final void writeBinaryTo(OutputStream output) throws IOException {
    try (Serializer serializer = new ProtoSerializer(output)) {
      writeTo(serializer);
    }
  }

  /** Marshals into the {@link OutputStream} in proto JSON format. */
  public final void writeJsonTo(OutputStream output) throws IOException {
    try (JsonSerializer serializer =
        new JsonSerializer(resolveJsonProvider().createJsonWriter(output))) {
      serializer.writeMessageValue(this);
    }
  }

  /** Marshals into the {@link JsonWriter} in proto JSON format. */
  public final void writeJsonToWriter(JsonWriter output) throws IOException {
    JsonSerializer serializer = new JsonSerializer(output);
    serializer.writeMessageValue(this);
  }

  /** Marshals into the {@link JsonWriter} in proto JSON format and adds a newline. */
  public final void writeJsonWithNewline(JsonWriter output) throws IOException {
    JsonSerializer serializer = new JsonSerializer(output);
    serializer.writeMessageValue(this);
    output.writeRaw('\n');
  }

  /** Returns the number of bytes this Marshaler will write in proto binary format. */
  public abstract int getBinarySerializedSize();

  protected abstract void writeTo(Serializer output) throws IOException;

  public MessageWriter toJsonMessageWriter() {
    return new MessageWriter() {
      @Override
      public void writeMessage(OutputStream output) throws IOException {
        writeJsonTo(output);
      }

      @Override
      public int getContentLength() {
        return -1;
      }
    };
  }

  public MessageWriter toBinaryMessageWriter() {
    return new MessageWriter() {
      @Override
      public void writeMessage(OutputStream output) throws IOException {
        writeBinaryTo(output);
      }

      @Override
      public int getContentLength() {
        return getBinarySerializedSize();
      }
    };
  }

  private static JsonProvider resolveJsonProvider() {
    if (jsonProvider == null) {
      synchronized (lock) {
        if (jsonProvider == null) {
          jsonProvider =
              JsonProviderUtil.resolveJsonProvider(
                  ComponentLoader.forClassLoader(Marshaler.class.getClassLoader()));
        }
      }
    }
    return jsonProvider;
  }
}
