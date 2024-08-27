/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class StreamJsonWriter implements JsonWriter {

  private static final Logger internalLogger = Logger.getLogger(StreamJsonWriter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final String type;
  private final OutputStream originalStream;
  private final OutputStream outputStream;

  public StreamJsonWriter(OutputStream originalStream, String type) {
    this.originalStream = originalStream;
    this.outputStream = new IgnoreCloseOutputStream(originalStream);
    this.type = type;
  }

  @Override
  public CompletableResultCode write(Marshaler exportRequest) {
    try {
      exportRequest.writeJsonTo(outputStream);
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

  @Override
  public String toString() {
    return "StreamJsonWriter{" + "outputStream=" + getName(originalStream) + '}';
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
