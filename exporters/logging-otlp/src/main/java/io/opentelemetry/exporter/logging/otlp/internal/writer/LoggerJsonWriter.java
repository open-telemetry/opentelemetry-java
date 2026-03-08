/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import static io.opentelemetry.exporter.logging.otlp.internal.writer.JsonUtil.JSON_FACTORY;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
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
  private final boolean prettyPrint;

  public LoggerJsonWriter(Logger logger, String type) {
    this(logger, type, /* prettyPrint= */ false);
  }

  public LoggerJsonWriter(Logger logger, String type, boolean prettyPrint) {
    this.logger = logger;
    this.type = type;
    this.prettyPrint = prettyPrint;
  }

  @Override
  public CompletableResultCode write(Marshaler exportRequest) {
    SegmentedStringWriter sw = new SegmentedStringWriter(JSON_FACTORY._getBufferRecycler());
    try (JsonGenerator gen = JsonUtil.create(sw)) {
      if (prettyPrint) {
        gen.useDefaultPrettyPrinter();
      }
      exportRequest.writeJsonToGenerator(gen);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to write OTLP JSON " + type, e);
      return CompletableResultCode.ofFailure();
    }

    try {
      logger.log(Level.INFO, sw.getAndClear());
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
