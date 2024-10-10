/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.traces;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.OtlpStdoutExporterBuilderUtil;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.LoggerJsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.StreamJsonWriter;
import io.opentelemetry.sdk.common.export.MemoryMode;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Builder for {@link OtlpJsonLoggingSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutSpanExporterBuilder {

  private static final String TYPE = "spans";

  private final Logger logger;
  private JsonWriter jsonWriter;
  private boolean wrapperJsonObject = true;
  private MemoryMode memoryMode = MemoryMode.IMMUTABLE_DATA;

  public OtlpStdoutSpanExporterBuilder(Logger logger) {
    this.logger = logger;
    this.jsonWriter = new LoggerJsonWriter(logger, TYPE);
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceSpans"
   *     object.
   */
  public OtlpStdoutSpanExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link MemoryMode#IMMUTABLE_DATA}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   */
  public OtlpStdoutSpanExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    this.memoryMode = memoryMode;
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * <p>The output stream will be closed when {@link OtlpStdoutSpanExporter#shutdown()} is called
   * unless it's {@link System#out} or {@link System#err}.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpStdoutSpanExporterBuilder setOutput(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.jsonWriter = new StreamJsonWriter(outputStream, TYPE);
    return this;
  }

  /** Sets the exporter to use the specified logger. */
  public OtlpStdoutSpanExporterBuilder setOutput(Logger logger) {
    requireNonNull(logger, "logger");
    this.jsonWriter = new LoggerJsonWriter(logger, TYPE);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpStdoutSpanExporter build() {
    OtlpStdoutExporterBuilderUtil.validate(memoryMode, wrapperJsonObject);
    return new OtlpStdoutSpanExporter(logger, jsonWriter, wrapperJsonObject, memoryMode);
  }
}
