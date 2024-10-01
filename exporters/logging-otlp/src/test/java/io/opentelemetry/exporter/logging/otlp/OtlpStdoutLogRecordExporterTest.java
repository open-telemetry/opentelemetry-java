/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.annotation.Nullable;

class OtlpStdoutLogRecordExporterTest
    extends AbstractOtlpStdoutExporterTest<OtlpStdoutLogRecordExporter> {

  public OtlpStdoutLogRecordExporterTest() {
    super(
        TestDataExporter.forLogs(),
        OtlpStdoutLogRecordExporter.class,
        ConfigurableLogRecordExporterProvider.class,
        LogRecordExporter.class,
        "OtlpStdoutLogRecordExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, wrapperJsonObject=true}");
  }

  @Override
  protected OtlpStdoutLogRecordExporter createDefaultExporter() {
    return OtlpStdoutLogRecordExporter.builder().build();
  }

  @Override
  protected OtlpStdoutLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpStdoutLogRecordExporterBuilder builder =
        OtlpStdoutLogRecordExporter.builder().setWrapperJsonObject(wrapperJsonObject);
    if (outputStream != null) {
      builder.setOutput(outputStream);
    } else {
      builder.setOutput(Logger.getLogger(exporterClass.getName()));
    }
    return builder.build();
  }
}
