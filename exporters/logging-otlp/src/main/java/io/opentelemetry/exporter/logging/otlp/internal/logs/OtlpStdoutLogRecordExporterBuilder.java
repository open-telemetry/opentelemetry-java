/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.LoggerJsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.StreamJsonWriter;
import io.opentelemetry.sdk.common.export.MemoryMode;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Builder for {@link OtlpJsonLoggingLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutLogRecordExporterBuilder {

  private static final String TYPE = "log records";

  private final Logger logger;
  @Nullable private OutputStream outputStream;
  @Nullable private Logger outputLogger;
  private boolean wrapperJsonObject = true;
  private MemoryMode memoryMode = MemoryMode.IMMUTABLE_DATA;
  private boolean prettyPrint;

  public OtlpStdoutLogRecordExporterBuilder(Logger logger) {
    this.logger = logger;
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceLogs"
   *     object.
   */
  public OtlpStdoutLogRecordExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link MemoryMode#IMMUTABLE_DATA}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   */
  public OtlpStdoutLogRecordExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    this.memoryMode = memoryMode;
    return this;
  }

  /** Sets the exporter to use pretty-printed JSON output. */
  public OtlpStdoutLogRecordExporterBuilder setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * <p>The output stream will be closed when {@link OtlpStdoutLogRecordExporter#shutdown()} is
   * called unless it's {@link System#out} or {@link System#err}.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpStdoutLogRecordExporterBuilder setOutput(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.outputStream = outputStream;
    this.outputLogger = null;
    return this;
  }

  /** Sets the exporter to use the specified logger. */
  public OtlpStdoutLogRecordExporterBuilder setOutput(Logger logger) {
    requireNonNull(logger, "logger");
    this.outputLogger = logger;
    this.outputStream = null;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpStdoutLogRecordExporter build() {
    if (memoryMode == MemoryMode.REUSABLE_DATA && !wrapperJsonObject) {
      throw new IllegalArgumentException(
          "Reusable data mode is not supported without wrapperJsonObject");
    }
    JsonWriter jsonWriter;
    if (outputStream != null) {
      jsonWriter = new StreamJsonWriter(outputStream, TYPE, prettyPrint);
    } else {
      Logger writerLogger = outputLogger != null ? outputLogger : this.logger;
      jsonWriter = new LoggerJsonWriter(writerLogger, TYPE, prettyPrint);
    }
    return new OtlpStdoutLogRecordExporter(logger, jsonWriter, wrapperJsonObject, memoryMode);
  }
}
