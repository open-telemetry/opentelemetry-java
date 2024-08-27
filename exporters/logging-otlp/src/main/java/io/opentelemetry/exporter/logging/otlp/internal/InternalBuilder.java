/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.InternalMetricBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.LoggerJsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.StreamJsonWriter;
import io.opentelemetry.sdk.common.export.MemoryMode;
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
  private MemoryMode memoryMode = MemoryMode.IMMUTABLE_DATA;
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

  public static InternalMetricBuilder forMetrics() {
    return new InternalMetricBuilder(
        Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName()), "metrics");
  }

  public static InternalBuilder forSpans() {
    return new InternalBuilder(
        Logger.getLogger(OtlpJsonLoggingSpanExporter.class.getName()), "spans");
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

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }

  public InternalBuilder setMemoryMode(MemoryMode memoryMode) {
    requireNonNull(memoryMode, "memoryMode");
    this.memoryMode = memoryMode;
    return this;
  }

  public boolean isWrapperJsonObject() {
    if (memoryMode == MemoryMode.REUSABLE_DATA && !wrapperJsonObject) {
      throw new IllegalArgumentException(
          "Reusable data mode is not supported without wrapperJsonObject");
    }
    return wrapperJsonObject;
  }

  public InternalBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }
}
