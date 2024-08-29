/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpJsonLoggingLogRecordExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@SuppressLogger(LogRecordExporter.class)
class LogRecordExporterTest extends AbstractOtlpJsonLoggingExporterTest<LogRecordExporter> {

  private static final LogRecordData LOG1 =
      TestLogRecordData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation")
                  .setVersion("1")
                  .setAttributes(Attributes.builder().put("key", "value").build())
                  .build())
          .setBody("body1")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setTimestamp(100L, TimeUnit.NANOSECONDS)
          .setObservedTimestamp(200L, TimeUnit.NANOSECONDS)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345876",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  private static final LogRecordData LOG2 =
      TestLogRecordData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
          .setBody("body2")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setTimestamp(100L, TimeUnit.NANOSECONDS)
          .setObservedTimestamp(200L, TimeUnit.NANOSECONDS)
          .setAttributes(Attributes.of(booleanKey("important"), true))
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345875",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  public LogRecordExporterTest() {
    super(
        OtlpJsonLoggingLogRecordExporter.class,
        ConfigurableLogRecordExporterProvider.class,
        LogRecordExporter.class,
        "expected-logs.json",
        "expected-logs-wrapper.json",
        "OtlpJsonLoggingLogRecordExporter{wrapperJsonObject=false, jsonWriter=LoggerJsonWriter}");
  }

  @Override
  protected LogRecordExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpStdoutLogRecordExporterBuilder builder = OtlpStdoutLogRecordExporterBuilder.create();
    if (outputStream != null) {
      builder.setOutputStream(outputStream);
    }
    return builder.setWrapperJsonObject(wrapperJsonObject).build();
  }

  @Override
  protected LogRecordExporter createDefaultExporter() {
    return OtlpJsonLoggingLogRecordExporter.create();
  }

  @Override
  protected LogRecordExporter createDefaultStdoutExporter() {
    return OtlpStdoutLogRecordExporter.create();
  }

  @Override
  protected LogRecordExporter toBuilderAndBack(LogRecordExporter exporter) {
    if (exporter instanceof OtlpStdoutLogRecordExporter) {
      OtlpStdoutLogRecordExporter otlpStdoutLogRecordExporter =
          (OtlpStdoutLogRecordExporter) exporter;
      return otlpStdoutLogRecordExporter.toBuilder().build();
    }
    return OtlpJsonLoggingLogRecordExporterBuilder.createFromExporter(
            (OtlpJsonLoggingLogRecordExporter) exporter)
        .build();
  }

  @Override
  protected CompletableResultCode export(LogRecordExporter exporter) {
    return exporter.export(Arrays.asList(LOG1, LOG2));
  }

  @Override
  protected CompletableResultCode flush(LogRecordExporter exporter) {
    return exporter.flush();
  }

  @Override
  protected CompletableResultCode shutdown(LogRecordExporter exporter) {
    return exporter.shutdown();
  }
}
