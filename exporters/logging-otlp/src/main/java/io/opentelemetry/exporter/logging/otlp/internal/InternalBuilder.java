/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.LoggerJsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.StreamJsonWriter;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Internal builder for configuring OTLP JSON logging exporters.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class InternalBuilder {
  private final Logger logger;
  private final String type;
  private JsonWriter jsonWriter;
  private boolean wrapperJsonObject = false;

  public InternalBuilder(Logger logger, String type) {
    this.logger = logger;
    this.type = type;
    this.jsonWriter = new LoggerJsonWriter(logger, type);
  }

  public static InternalBuilder forLogs() {
    return new InternalBuilder(
        Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName()), "log records");
  }

  public JsonWriter getJsonWriter() {
    return jsonWriter;
  }

  public InternalBuilder setJsonWriter(JsonWriter jsonWriter) {
    this.jsonWriter = jsonWriter;
    return this;
  }

  public InternalBuilder setOutputStream(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.jsonWriter = new StreamJsonWriter(outputStream, type);
    return this;
  }

  public InternalBuilder setUseLogger() {
    this.jsonWriter = new LoggerJsonWriter(logger, type);
    return this;
  }

  public boolean isWrapperJsonObject() {
    return wrapperJsonObject;
  }

  public InternalBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }
}
