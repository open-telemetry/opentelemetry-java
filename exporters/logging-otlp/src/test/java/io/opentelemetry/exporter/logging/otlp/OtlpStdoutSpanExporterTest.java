/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.traces.OtlpStdoutSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.traces.OtlpStdoutSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.annotation.Nullable;

class OtlpStdoutSpanExporterTest extends AbstractOtlpStdoutExporterTest<OtlpStdoutSpanExporter> {

  public OtlpStdoutSpanExporterTest() {
    super(
        TestDataExporter.forSpans(),
        OtlpStdoutSpanExporter.class,
        ConfigurableSpanExporterProvider.class,
        SpanExporter.class,
        "OtlpStdoutSpanExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, useLowAllocation=true, memoryMode=IMMUTABLE_DATA}");
  }

  @Override
  protected OtlpStdoutSpanExporter createDefaultExporter() {
    return OtlpStdoutSpanExporter.builder().build();
  }

  @Override
  protected OtlpStdoutSpanExporter createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean useLowAllocation) {
    OtlpStdoutSpanExporterBuilder builder =
        OtlpStdoutSpanExporter.builder()
            .setMemoryMode(memoryMode)
            .setWrapperJsonObject(useLowAllocation);
    if (outputStream != null) {
      builder.setOutput(outputStream);
    } else {
      builder.setOutput(Logger.getLogger(exporterClass.getName()));
    }
    return builder.build();
  }
}
