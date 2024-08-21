/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.logging.otlp.internal.trace.OtlpJsonLoggingSpanExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.trace.OtlpStdoutSpanExporter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nullable;

@SuppressLogger(OtlpJsonLoggingSpanExporter.class)
class SpanExporterTest extends AbstractOtlpJsonLoggingExporterTest<OtlpJsonLoggingSpanExporter> {

  private static final SpanData SPAN1 =
      TestSpanData.builder()
          .setHasEnded(true)
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654321",
                  "8765432112345678",
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(100)
          .setEndEpochNanos(100 + 1000)
          .setStatus(StatusData.ok())
          .setName("testSpan1")
          .setKind(SpanKind.INTERNAL)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setEvents(
              Collections.singletonList(
                  EventData.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalAttributeCount(2)
          .setTotalRecordedEvents(1)
          .setTotalRecordedLinks(0)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation")
                  .setVersion("1")
                  .setAttributes(Attributes.builder().put("key", "value").build())
                  .build())
          .setResource(RESOURCE)
          .build();

  private static final SpanData SPAN2 =
      TestSpanData.builder()
          .setHasEnded(false)
          .setSpanContext(
              SpanContext.create(
                  "12340000000043211234000000004321",
                  "8765000000005678",
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(500)
          .setEndEpochNanos(500 + 1001)
          .setStatus(StatusData.error())
          .setName("testSpan2")
          .setKind(SpanKind.CLIENT)
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
          .build();

  public SpanExporterTest() {
    super(
        OtlpJsonLoggingSpanExporter.class,
        ConfigurableSpanExporterProvider.class,
        SpanExporter.class,
        "expected-spans.json",
        "expected-spans-wrapper.json",
        "OtlpJsonLoggingSpanExporter{memoryMode=IMMUTABLE_DATA, wrapperJsonObject=false, jsonWriter=LoggerJsonWriter}");
  }

  @Override
  protected OtlpJsonLoggingSpanExporter createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject) {
    OtlpJsonLoggingSpanExporterBuilder builder = OtlpStdoutSpanExporter.builder();
    if (outputStream == null) {
      builder.setUseLogger();
    } else {
      builder.setOutputStream(outputStream);
    }

    return builder.setMemoryMode(memoryMode).setWrapperJsonObject(wrapperJsonObject).build();
  }

  @Override
  protected OtlpJsonLoggingSpanExporter createDefaultExporter() {
    return (OtlpJsonLoggingSpanExporter) OtlpJsonLoggingSpanExporter.create();
  }

  @Override
  protected OtlpJsonLoggingSpanExporter createDefaultStdoutExporter() {
    return OtlpStdoutSpanExporter.create();
  }

  @Override
  protected OtlpJsonLoggingSpanExporter toBuilderAndBack(OtlpJsonLoggingSpanExporter exporter) {
    return OtlpJsonLoggingSpanExporterBuilder.createFromExporter(exporter).build();
  }

  @Override
  protected CompletableResultCode export(OtlpJsonLoggingSpanExporter exporter) {
    return exporter.export(Arrays.asList(SPAN1, SPAN2));
  }

  @Override
  protected CompletableResultCode flush(OtlpJsonLoggingSpanExporter exporter) {
    return exporter.flush();
  }

  @Override
  protected CompletableResultCode shutdown(OtlpJsonLoggingSpanExporter exporter) {
    return exporter.shutdown();
  }
}
