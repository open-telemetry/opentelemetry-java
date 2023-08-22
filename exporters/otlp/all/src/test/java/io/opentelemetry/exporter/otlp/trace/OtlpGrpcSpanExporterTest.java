/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.Closeable;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcSpanExporterTest extends AbstractGrpcTelemetryExporterTest<SpanData, ResourceSpans> {

  OtlpGrpcSpanExporterTest() {
    super("span", ResourceSpans.getDefaultInstance());
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcSpanExporter.builder().build()) {
      assertThat(exporter).extracting("delegate.grpcSender").isInstanceOf(OkHttpGrpcSender.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcSpanExporter.builder());
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> toBuilder(TelemetryExporter<SpanData> exporter) {
    return TelemetryExporterBuilder.wrap(((OtlpGrpcSpanExporter) exporter.unwrap()).toBuilder());
  }

  @Override
  protected SpanData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeSpanData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<SpanData> telemetry) {
    return ResourceSpansMarshaler.create(telemetry);
  }
}
