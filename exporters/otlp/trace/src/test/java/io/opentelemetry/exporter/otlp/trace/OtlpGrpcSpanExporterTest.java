/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class OtlpGrpcSpanExporterTest
    extends AbstractGrpcTelemetryExporterTest<SpanData, ResourceSpans, OtlpGrpcSpanExporter> {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  OtlpGrpcSpanExporterTest() {
    super("span", ResourceSpans.getDefaultInstance());
  }

  @Override
  protected OtlpGrpcSpanExporter createExporter(String endpoint) {
    return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
  }

  @Override
  protected OtlpGrpcSpanExporter createExporterWithTimeout(String endpoint, Duration timeout) {
    return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).setTimeout(timeout).build();
  }

  @Override
  protected CompletableResultCode shutdownExporter(OtlpGrpcSpanExporter exporter) {
    return exporter.shutdown();
  }

  @Override
  protected CompletableResultCode doExport(
      OtlpGrpcSpanExporter exporter, List<SpanData> telemetry) {
    return exporter.export(telemetry);
  }

  @Override
  protected SpanData generateFakeTelemetry() {
    long duration = TimeUnit.MILLISECONDS.toNanos(900);
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + duration;
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(startNs)
        .setEndEpochNanos(endNs)
        .setStatus(StatusData.ok())
        .setKind(SpanKind.SERVER)
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .setInstrumentationLibraryInfo(
            InstrumentationLibraryInfo.create("testLib", "1.0", "http://url"))
        .build();
  }

  @Override
  protected Marshaler[] toMarshalers(List<SpanData> telemetry) {
    return ResourceSpansMarshaler.create(telemetry);
  }
}
