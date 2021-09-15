/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Marshaler from an SDK structure to protobuf wire format.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class Marshaler {

  /** Marshals into the {@link OutputStream} in proto binary format. */
  public final void writeBinaryTo(OutputStream output) throws IOException {
    try (Serializer serializer = new ProtoSerializer(output)) {
      writeTo(serializer);
    }
  }

  /** Marshals into the {@link OutputStream} in proto JSON format. */
  public final void writeJsonTo(OutputStream output) throws IOException {
    try (JsonSerializer serializer = new JsonSerializer(output)) {
      serializer.writeMessageValue(this);
    }
  }

  /** Marshals into the {@link JsonGenerator} in proto JSON format. */
  public final void writeJsonTo(JsonGenerator output) throws IOException {
    try (JsonSerializer serializer = new JsonSerializer(output)) {
      serializer.writeMessageValue(this);
    }
  }

  /** Returns the number of bytes this Marshaler will write in proto binary format. */
  public abstract int getBinarySerializedSize();

  protected abstract void writeTo(Serializer output) throws IOException;
}
