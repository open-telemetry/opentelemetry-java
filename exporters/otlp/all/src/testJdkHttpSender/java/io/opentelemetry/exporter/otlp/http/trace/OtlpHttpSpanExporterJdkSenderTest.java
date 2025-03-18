/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.HttpSpanExporterBuilderWrapper;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSender;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpHttpSpanExporterJdkSenderTest
    extends AbstractHttpTelemetryExporterTest<SpanData, ResourceSpans> {

  protected OtlpHttpSpanExporterJdkSenderTest() {
    super("span", "/v1/traces", ResourceSpans.getDefaultInstance());
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> exporterBuilder() {
    return new HttpSpanExporterBuilderWrapper(OtlpHttpSpanExporter.builder());
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> toBuilder(TelemetryExporter<SpanData> exporter) {
    return new HttpSpanExporterBuilderWrapper(
        ((OtlpHttpSpanExporter) exporter.unwrap()).toBuilder());
  }

  @Test
  void isJdkHttpSender() {
    TelemetryExporter<SpanData> exporter = exporterBuilder().build();

    try {
      assertThat(exporter.unwrap())
          .extracting("delegate")
          .extracting("httpSender")
          .isInstanceOf(JdkHttpSender.class);
    } finally {
      exporter.shutdown();
    }
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
