/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class StreamJsonWriter implements JsonWriter {

  public static final JsonFactory JSON_FACTORY = new JsonFactory();

  private static final Logger internalLogger = Logger.getLogger(StreamJsonWriter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final String type;
  private final OutputStream outputStream;

  public StreamJsonWriter(OutputStream originalStream, String type) {
    this.outputStream = originalStream;
    this.type = type;
  }

  @Override
  public CompletableResultCode write(Marshaler exportRequest) {
    try {
      exportRequest.writeJsonWithNewline(
          JSON_FACTORY
              .createGenerator(outputStream)
              .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
      return CompletableResultCode.ofSuccess();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to write OTLP JSON " + type, e);
      return CompletableResultCode.ofFailure();
    }
  }

  @Override
  public CompletableResultCode flush() {
    try {
      outputStream.flush();
      return CompletableResultCode.ofSuccess();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to flush items", e);
      return CompletableResultCode.ofFailure();
    }
  }

  @SuppressWarnings("SystemOut")
  @Override
  public CompletableResultCode close() {
    if (outputStream == System.out || outputStream == System.err) {
      // closing System.out or System.err is not allowed - it breaks the output stream
      return CompletableResultCode.ofSuccess();
    }
    try {
      outputStream.close();
      return CompletableResultCode.ofSuccess();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to close stream", e);
      return CompletableResultCode.ofFailure();
    }
  }

  @Override
  public String toString() {
    return "StreamJsonWriter{" + "outputStream=" + getName(outputStream) + '}';
  }

  @SuppressWarnings("SystemOut")
  private static String getName(OutputStream outputStream) {
    if (outputStream == System.out) {
      return "stdout";
    }
    if (outputStream == System.err) {
      return "stderr";
    }
    return outputStream.toString();
  }
}
