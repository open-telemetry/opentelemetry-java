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
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Builder for {@link OtlpJsonLoggingLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpJsonLoggingLogRecordExporterBuilder {

  private static final String TYPE = "log records";
  private JsonWriter jsonWriter;
  private boolean wrapperJsonObject;

  OtlpJsonLoggingLogRecordExporterBuilder(JsonWriter jsonWriter, boolean wrapperJsonObject) {
    this.jsonWriter = jsonWriter;
    this.wrapperJsonObject = wrapperJsonObject;
  }

  /**
   * Creates a new {@link OtlpJsonLoggingLogRecordExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingLogRecordExporterBuilder}.
   */
  public static OtlpJsonLoggingLogRecordExporterBuilder create() {
    return new OtlpJsonLoggingLogRecordExporterBuilder(
        new LoggerJsonWriter(
            Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName()), TYPE),
        /* wrapperJsonObject= */ false);
  }

  /**
   * Creates a new {@link OtlpJsonLoggingLogRecordExporterBuilder} from an existing exporter.
   *
   * @param exporter the existing exporter.
   * @return a new {@link OtlpJsonLoggingLogRecordExporterBuilder}.
   */
  public static OtlpJsonLoggingLogRecordExporterBuilder createFromExporter(
      OtlpJsonLoggingLogRecordExporter exporter) {
    LogRecordBuilderAccessUtil.Argument argument =
        LogRecordBuilderAccessUtil.getToBuilder().apply(exporter);
    return new OtlpJsonLoggingLogRecordExporterBuilder(
        argument.getJsonWriter(), argument.isWrapperJsonObject());
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceLogs"
   *     object.
   */
  public OtlpJsonLoggingLogRecordExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpJsonLoggingLogRecordExporterBuilder setOutputStream(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.jsonWriter = new StreamJsonWriter(outputStream, TYPE);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpJsonLoggingLogRecordExporter build() {
    return LogRecordBuilderAccessUtil.getToExporter()
        .apply(new LogRecordBuilderAccessUtil.Argument(jsonWriter, wrapperJsonObject));
  }
}
