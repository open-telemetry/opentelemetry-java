/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.traces;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.LoggerJsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.StreamJsonWriter;
import io.opentelemetry.sdk.common.export.MemoryMode;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Builder for {@link OtlpJsonLoggingSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutSpanExporterBuilder {

  private static final String TYPE = "spans";

  private final Logger logger;
  @Nullable private OutputStream outputStream;
  @Nullable private Logger outputLogger;
  private Function<OutputStream, OutputStream> outputStreamMapper = Function.identity();
  private boolean wrapperJsonObject = true;
  private MemoryMode memoryMode = MemoryMode.IMMUTABLE_DATA;

  public OtlpStdoutSpanExporterBuilder(Logger logger) {
    this.logger = logger;
    this.outputLogger = logger;
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
    this.outputStream = outputStream;
    this.outputLogger = null;
    return this;
  }

  /** Sets the exporter to use the specified logger. */
  public OtlpStdoutSpanExporterBuilder setOutput(Logger logger) {
    requireNonNull(logger, "logger");
    this.outputLogger = logger;
    this.outputStream = null;
    return this;
  }

  /**
   * Sets the {@link Function} to map the output stream before writing.
   *
   * <p>This can be used to customize the stream, for example to apply compression:
   *
   * <pre>{@code
   * builder.setOutputStreamMapper(GZIPOutputStream::new)
   * }</pre>
   *
   * @param outputStreamMapper the function to map the output stream
   * @return this
   */
  public OtlpStdoutSpanExporterBuilder setOutputStreamMapper(
      Function<OutputStream, OutputStream> outputStreamMapper) {
    requireNonNull(outputStreamMapper, "outputStreamMapper");
    this.outputStreamMapper = outputStreamMapper;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpStdoutSpanExporter build() {
    if (memoryMode == MemoryMode.REUSABLE_DATA && !wrapperJsonObject) {
      throw new IllegalArgumentException(
          "Reusable data mode is not supported without wrapperJsonObject");
    }
    JsonWriter jsonWriter;
    if (outputStream != null) {
      jsonWriter =
          new StreamJsonWriter(
              requireNonNull(
                  outputStreamMapper.apply(outputStream), "outputStreamMapper returned null"),
              TYPE);
    } else {
      jsonWriter = new LoggerJsonWriter(requireNonNull(outputLogger), TYPE);
    }
    return new OtlpStdoutSpanExporter(logger, jsonWriter, wrapperJsonObject, memoryMode);
  }
}
