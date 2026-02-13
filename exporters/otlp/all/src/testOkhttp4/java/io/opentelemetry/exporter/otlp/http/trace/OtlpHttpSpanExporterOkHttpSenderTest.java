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
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpHttpSpanExporterOkHttpSenderTest
    extends AbstractHttpTelemetryExporterTest<SpanData, ResourceSpans> {

  protected OtlpHttpSpanExporterOkHttpSenderTest() {
    super("span", "/v1/traces", ResourceSpans.getDefaultInstance());
  }

  /** Test configuration specific to metric exporter. */
  @Test
  void stringRepresentation() {
    try (SpanExporter spanExporter = OtlpHttpSpanExporter.builder().build()) {
      assertThat(spanExporter.toString())
          .matches(
              "OtlpHttpSpanExporter\\{"
                  + "endpoint=http://localhost:4318/v1/traces, "
                  + "timeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "proxyOptions=null, "
                  + "compressorEncoding=null, "
                  + "connectTimeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "exportAsJson=false, "
                  + "headers=Headers\\{User-Agent=OBFUSCATED\\}, "
                  + "retryPolicy=RetryPolicy\\{.*\\}, "
                  + "componentLoader=.*, "
                  + "exporterType=OTLP_HTTP_SPAN_EXPORTER, "
                  + "internalTelemetrySchemaVersion=LEGACY, "
                  + "memoryMode=REUSABLE_DATA"
                  + "\\}");
    }
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

  @Override
  protected SpanData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeSpanData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<SpanData> telemetry) {
    return ResourceSpansMarshaler.create(telemetry);
  }
}
