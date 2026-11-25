/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.OutputStream;
import java.util.function.Function;
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
        "OtlpStdoutLogRecordExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, wrapperJsonObject=true, memoryMode=IMMUTABLE_DATA}");
  }

  @Override
  protected OtlpStdoutLogRecordExporter createDefaultExporter() {
    return OtlpStdoutLogRecordExporter.builder().build();
  }

  @Override
  protected OtlpStdoutLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject) {
    OtlpStdoutLogRecordExporterBuilder builder =
        OtlpStdoutLogRecordExporter.builder()
            .setMemoryMode(memoryMode)
            .setWrapperJsonObject(wrapperJsonObject);
    if (outputStream != null) {
      builder.setOutput(outputStream);
    } else {
      builder.setOutput(Logger.getLogger(exporterClass.getName()));
    }
    return builder.build();
  }

  @Override
  protected OtlpStdoutLogRecordExporter createExporter(
      OutputStream outputStream, Function<OutputStream, OutputStream> outputStreamMapper) {
    return OtlpStdoutLogRecordExporter.builder()
        .setOutput(outputStream)
        .setOutputStreamMapper(outputStreamMapper)
        .build();
  }

  @Override
  protected OtlpStdoutLogRecordExporter createExporterWithMapperBeforeOutput(
      OutputStream outputStream, Function<OutputStream, OutputStream> outputStreamMapper) {
    return OtlpStdoutLogRecordExporter.builder()
        .setOutputStreamMapper(outputStreamMapper)
        .setOutput(outputStream)
        .build();
  }
}
