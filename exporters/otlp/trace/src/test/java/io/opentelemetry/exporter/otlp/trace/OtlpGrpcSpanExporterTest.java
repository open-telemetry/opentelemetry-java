/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

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

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setCompression("gzip"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcSpanExporter.builder().setCompression("none"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> OtlpGrpcSpanExporter.builder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpGrpcSpanExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  @Test
  void usingOkHttp() {
    assertThat(OtlpGrpcSpanExporter.builder().delegate)
        .isInstanceOf(OkHttpGrpcExporterBuilder.class);
  }
}
