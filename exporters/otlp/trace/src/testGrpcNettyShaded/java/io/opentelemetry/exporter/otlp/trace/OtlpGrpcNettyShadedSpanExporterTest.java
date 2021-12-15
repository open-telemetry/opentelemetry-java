/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpGrpcNettyShadedSpanExporterTest
    extends AbstractGrpcTelemetryExporterTest<SpanData, ResourceSpans> {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  OtlpGrpcNettyShadedSpanExporterTest() {
    super("span", ResourceSpans.getDefaultInstance());
  }

  @Test
  void usingGrpc() {
    assertThat(OtlpGrpcSpanExporter.builder().delegate)
        .isInstanceOf(DefaultGrpcExporterBuilder.class);
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> exporterBuilder() {
    OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder();
    return new TelemetryExporterBuilder<SpanData>() {
      @Override
      public TelemetryExporterBuilder<SpanData> setEndpoint(String endpoint) {
        builder.setEndpoint(endpoint);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> setTimeout(long timeout, TimeUnit unit) {
        builder.setTimeout(timeout, unit);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> setTimeout(Duration timeout) {
        builder.setTimeout(timeout);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> setCompression(String compression) {
        builder.setCompression(compression);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> addHeader(String key, String value) {
        builder.addHeader(key, value);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> setTrustedCertificates(byte[] certificates) {
        builder.setTrustedCertificates(certificates);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> addRetryPolicy(RetryPolicy retryPolicy) {
        builder.delegate.addRetryPolicy(retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporter<SpanData> build() {
        return TelemetryExporter.wrap(builder.build());
      }
    };
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
