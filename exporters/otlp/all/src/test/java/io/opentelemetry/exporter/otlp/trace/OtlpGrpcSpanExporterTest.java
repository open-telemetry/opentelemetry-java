/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpGrpcSpanExporterTest extends AbstractGrpcTelemetryExporterTest<SpanData, ResourceSpans> {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  OtlpGrpcSpanExporterTest() {
    super("span", ResourceSpans.getDefaultInstance());
  }

  @Test
  void testSetRetryPolicyOnDelegate() {
    assertThatCode(
            () ->
                RetryUtil.setRetryPolicyOnDelegate(
                    OtlpGrpcSpanExporter.builder(), RetryPolicy.getDefault()))
        .doesNotThrowAnyException();
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcSpanExporter.builder().build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(OkHttpGrpcExporter.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcSpanExporter.builder());
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
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.create("testLib", "1.0", "http://url"))
        .build();
  }

  @Override
  protected Marshaler[] toMarshalers(List<SpanData> telemetry) {
    return ResourceSpansMarshaler.create(telemetry);
  }
}
