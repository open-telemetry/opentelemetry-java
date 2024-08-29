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
  private final String type;
  private JsonWriter jsonWriter;
  private boolean wrapperJsonObject = false;

  public InternalBuilder(LoggerJsonWriter jsonWriter, String type) {
    this.type = type;
    this.jsonWriter = jsonWriter;
  }

  public static InternalBuilder forLogs() {
    Logger logger = Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName());
    String type = "log records";
    return new InternalBuilder(new LoggerJsonWriter(logger, type), type);
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

  public boolean isWrapperJsonObject() {
    return wrapperJsonObject;
  }

  public InternalBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }
}
