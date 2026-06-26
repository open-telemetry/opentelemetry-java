/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.JsonStringWriter;
import java.io.IOException;
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

  @Override
  public CompletableResultCode write(Marshaler exportRequest) {
    JsonStringWriter stringWriter;
    try {
      stringWriter = JsonProviderHolder.get().createJsonStringWriter();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to create JSON writer for " + type, e);
      return CompletableResultCode.ofFailure();
    }

    try (io.opentelemetry.sdk.common.export.JsonWriter writer = stringWriter.writer()) {
      exportRequest.writeJsonToWriter(writer);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to write OTLP JSON " + type, e);
      return CompletableResultCode.ofFailure();
    }

    try {
      logger.log(Level.INFO, stringWriter.getAndClear());
      return CompletableResultCode.ofSuccess();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to write OTLP JSON " + type, e);
      return CompletableResultCode.ofFailure();
    }
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
