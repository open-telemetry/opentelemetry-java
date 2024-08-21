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
import io.opentelemetry.exporter.logging.otlp.internal.logs.LoggingLogRecordExporterProvider;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpJsonLoggingLogRecordExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterComponentProvider;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterProvider;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@SuppressLogger(OtlpJsonLoggingLogRecordExporter.class)
class OtlpStdoutLogRecordExporterTest
    extends AbstractOtlpJsonLoggingExporterTest<OtlpJsonLoggingLogRecordExporter> {

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

  public OtlpStdoutLogRecordExporterTest() {
    super(
        OtlpJsonLoggingLogRecordExporter.class, "expected-logs.json", "expected-logs-wrapper.json");
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject) {
    OtlpJsonLoggingLogRecordExporterBuilder builder =
        OtlpJsonLoggingLogRecordExporterBuilder.create();
    if (outputStream == null) {
      builder.setUseLogger();
    } else {
      builder.setOutputStream(outputStream);
    }
    return builder.setMemoryMode(memoryMode).setWrapperJsonObject(wrapperJsonObject).build();
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createDefaultExporter() {
    return (OtlpJsonLoggingLogRecordExporter) OtlpJsonLoggingLogRecordExporter.create();
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createDefaultStdoutExporter() {
    return OtlpStdoutLogRecordExporter.create();
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createExporterWithProperties(
      ConfigProperties properties) {
    return (OtlpJsonLoggingLogRecordExporter)
        new LoggingLogRecordExporterProvider().createExporter(properties);
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createStdoutExporterWithProperties(
      ConfigProperties properties) {
    return (OtlpJsonLoggingLogRecordExporter)
        new OtlpStdoutLogRecordExporterProvider().createExporter(properties);
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter createStdoutExporterWithStructuredProperties(
      StructuredConfigProperties properties) {
    return (OtlpJsonLoggingLogRecordExporter)
        new OtlpStdoutLogRecordExporterComponentProvider().create(properties);
  }

  @Override
  protected OtlpJsonLoggingLogRecordExporter toBuilderAndBack(
      OtlpJsonLoggingLogRecordExporter exporter) {
    return OtlpJsonLoggingLogRecordExporterBuilder.createFromExporter(exporter).build();
  }

  @Override
  protected CompletableResultCode export(OtlpJsonLoggingLogRecordExporter exporter) {
    return exporter.export(Arrays.asList(LOG1, LOG2));
  }

  @Override
  protected CompletableResultCode shutdown(OtlpJsonLoggingLogRecordExporter exporter) {
    return exporter.shutdown();
  }
}
