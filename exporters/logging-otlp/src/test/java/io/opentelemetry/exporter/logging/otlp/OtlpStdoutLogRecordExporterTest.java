/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.OutputStream;
import javax.annotation.Nullable;

@SuppressLogger(LogRecordExporter.class)
class OtlpStdoutLogRecordExporterTest
    extends AbstractOtlpStdoutExporterTest<OtlpStdoutLogRecordExporter> {

  public OtlpStdoutLogRecordExporterTest() {
    super(
        "otlp-stdout",
        TestDataExporter.forLogs(),
        OtlpJsonLoggingLogRecordExporter.class,
        ConfigurableLogRecordExporterProvider.class,
        LogRecordExporter.class,
        "expected-logs.json",
        "expected-logs-wrapper.json",
        "OtlpStdoutLogRecordExporter{delegate=OtlpJsonLoggingLogRecordExporter{wrapperJsonObject=true, jsonWriter=StreamJsonWriter{outputStream=stdout}}}");
  }

  @Override
  protected OtlpStdoutLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpStdoutLogRecordExporterBuilder builder = OtlpStdoutLogRecordExporterBuilder.create();
    if (outputStream != null) {
      builder.setOutputStream(outputStream);
    }
    return builder.setWrapperJsonObject(wrapperJsonObject).build();
  }

  @Override
  protected OtlpStdoutLogRecordExporter createDefaultExporter() {
    return (OtlpStdoutLogRecordExporter) OtlpStdoutLogRecordExporter.create();
  }

  @Override
  protected OtlpStdoutLogRecordExporter toBuilderAndBack(OtlpStdoutLogRecordExporter exporter) {
    return exporter.toBuilder().build();
  }
}
