/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import com.fasterxml.jackson.core.JsonGenerator;
import io.opentelemetry.exporter.grpc.GrpcMessageWriter;
import io.opentelemetry.exporter.http.HttpRequestBodyWriter;
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
  // Intentionally not overloading writeJsonTo(OutputStream) in order to avoid compilation
  // dependency on jackson when using writeJsonTo(OutputStream). See:
  // https://github.com/open-telemetry/opentelemetry-java-contrib/pull/1551#discussion_r1849064365
  public final void writeJsonToGenerator(JsonGenerator output) throws IOException {
    try (JsonSerializer serializer = new JsonSerializer(output)) {
      serializer.writeMessageValue(this);
    }
  }

  /** Marshals into the {@link JsonGenerator} in proto JSON format and adds a newline. */
  public final void writeJsonWithNewline(JsonGenerator output) throws IOException {
    try (JsonSerializer serializer = new JsonSerializer(output)) {
      serializer.writeMessageValue(this);
      output.writeRaw('\n');
    }
  }

  /** Returns the number of bytes this Marshaler will write in proto binary format. */
  public abstract int getBinarySerializedSize();

  protected abstract void writeTo(Serializer output) throws IOException;

  public HttpRequestBodyWriter toHttpRequestBodyWriter(boolean exportAsJson) {
    return new HttpRequestBodyWriter() {
      @Override
      public void writeRequestBody(OutputStream output) throws IOException {
        if (exportAsJson) {
          writeJsonTo(output);
        } else {
          writeBinaryTo(output);
        }
      }

      @Override
      public int contentLength() {
        return exportAsJson ? -1 : getBinarySerializedSize();
      }
    };
  }

  public GrpcMessageWriter toGrpcRequestBodyWriter() {
    return new GrpcMessageWriter() {
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
}
