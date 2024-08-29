/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import java.io.OutputStream;

/**
 * Builder for {@link OtlpJsonLoggingLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutLogRecordExporterBuilder {

  private final OtlpJsonLoggingLogRecordExporterBuilder delegate;

  OtlpStdoutLogRecordExporterBuilder(OtlpJsonLoggingLogRecordExporterBuilder delegate) {
    this.delegate = delegate;
  }

  /**
   * Creates a new {@link OtlpStdoutLogRecordExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpStdoutLogRecordExporterBuilder}.
   */
  public static OtlpStdoutLogRecordExporterBuilder create() {
    return new OtlpStdoutLogRecordExporterBuilder(OtlpJsonLoggingLogRecordExporterBuilder.create());
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceLogs"
   *     object.
   */
  public OtlpStdoutLogRecordExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    delegate.setWrapperJsonObject(wrapperJsonObject);
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpStdoutLogRecordExporterBuilder setOutputStream(OutputStream outputStream) {
    delegate.setOutputStream(outputStream);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpStdoutLogRecordExporter build() {
    return new OtlpStdoutLogRecordExporter(delegate.build());
  }
}
