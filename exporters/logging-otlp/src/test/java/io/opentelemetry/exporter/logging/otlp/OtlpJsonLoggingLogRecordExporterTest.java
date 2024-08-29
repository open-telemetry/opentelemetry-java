/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpJsonLoggingLogRecordExporterBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.OutputStream;
import javax.annotation.Nullable;

@SuppressLogger(LogRecordExporter.class)
class OtlpJsonLoggingLogRecordExporterTest
    extends AbstractOtlpJsonLoggingExporterTest<OtlpJsonLoggingLogRecordExporter> {

  public OtlpJsonLoggingLogRecordExporterTest() {
    super(
        "logging-otlp",
        TestDataExporter.forLogs(),
        OtlpJsonLoggingLogRecordExporter.class,
        ConfigurableLogRecordExporterProvider.class,
        "expected-logs.json",
        "expected-logs-wrapper.json",
        "OtlpJsonLoggingLogRecordExporter{wrapperJsonObject=false, jsonWriter=LoggerJsonWriter}");
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpJsonLoggingLogRecordExporterBuilder builder =
        OtlpJsonLoggingLogRecordExporterBuilder.create();
    if (outputStream != null) {
      builder.setOutputStream(outputStream);
    }
    return builder.setWrapperJsonObject(wrapperJsonObject).build();
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createDefaultExporter() {
    return (OtlpJsonLoggingLogRecordExporter) OtlpJsonLoggingLogRecordExporter.create();
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter toBuilderAndBack(
      OtlpJsonLoggingLogRecordExporter exporter) {
    return OtlpJsonLoggingLogRecordExporterBuilder.createFromExporter(exporter).build();
  }
}
