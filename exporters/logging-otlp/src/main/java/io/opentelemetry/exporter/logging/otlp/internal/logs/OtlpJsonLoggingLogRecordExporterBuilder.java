/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import java.io.OutputStream;

/**
 * Builder for {@link OtlpJsonLoggingLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpJsonLoggingLogRecordExporterBuilder {

  private final InternalBuilder builder;

  OtlpJsonLoggingLogRecordExporterBuilder(InternalBuilder builder) {
    this.builder = builder;
  }

  /**
   * Creates a new {@link OtlpJsonLoggingLogRecordExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingLogRecordExporterBuilder}.
   */
  public static OtlpJsonLoggingLogRecordExporterBuilder create() {
    return new OtlpJsonLoggingLogRecordExporterBuilder(InternalBuilder.forLogs());
  }

  /**
   * Creates a new {@link OtlpJsonLoggingLogRecordExporterBuilder} from an existing exporter.
   *
   * @param exporter the existing exporter.
   * @return a new {@link OtlpJsonLoggingLogRecordExporterBuilder}.
   */
  public static OtlpJsonLoggingLogRecordExporterBuilder createFromExporter(
      OtlpJsonLoggingLogRecordExporter exporter) {
    return new OtlpJsonLoggingLogRecordExporterBuilder(
        LogRecordBuilderAccessUtil.getToBuilder().apply(exporter));
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceLogs"
   *     object.
   */
  public OtlpJsonLoggingLogRecordExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    builder.setWrapperJsonObject(wrapperJsonObject);
    return this;
  }

  /** Sets the exporter to use the logger for output. */
  public OtlpJsonLoggingLogRecordExporterBuilder setUseLogger() {
    builder.setUseLogger();
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpJsonLoggingLogRecordExporterBuilder setOutputStream(OutputStream outputStream) {
    builder.setOutputStream(outputStream);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpJsonLoggingLogRecordExporter build() {
    return LogRecordBuilderAccessUtil.getToExporter().apply(builder);
  }
}
