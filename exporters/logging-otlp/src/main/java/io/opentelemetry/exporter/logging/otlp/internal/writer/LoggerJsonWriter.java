/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class LoggerJsonWriter implements JsonWriter {

  private final Logger logger;
  private final String type;

  public LoggerJsonWriter(Logger logger, String type) {
    this.logger = logger;
    this.type = type;
  }

  // toString(String) decodes straight from the internal buffer, avoiding the array copy that
  // new String(bos.toByteArray(), UTF_8) makes. The toString(Charset) overload errorprone wants is
  // Java 10+, but this module targets Java 8.
  @SuppressWarnings("JdkObsolete")
  @Override
  public CompletableResultCode write(Marshaler exportRequest) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    String json;
    try {
      exportRequest.writeJsonTo(bos);
      // UTF-8 is always available; the declared UnsupportedEncodingException cannot occur.
      json = bos.toString(StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to write OTLP JSON " + type, e);
      return CompletableResultCode.ofFailure();
    }

    logger.log(Level.INFO, json);
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode close() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    return "LoggerJsonWriter";
  }
}
