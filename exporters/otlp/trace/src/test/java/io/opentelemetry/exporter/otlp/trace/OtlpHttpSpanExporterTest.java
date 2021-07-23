/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.internal.SpanAdapter;
import io.opentelemetry.exporter.otlp.trace.OtlpHttpSpanExporterBuilder.Encoding;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpHttpSpanExporterTest {

  @Rule private final OtlpHttpRule otlpHttp = new OtlpHttpRule();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpHttpSpanExporter.class);

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpHttpSpanExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported compression method. Supported compression methods include: gzip.");
  }

  @Test
  void testExportAsJsonUncompressed() {
    OtlpHttpSpanExporter exporter = builder().setJsonEncoding().build();
    exportAndVerify(exporter);
  }

  @Test
  void testExportAsJsonGzipCompressed() {
    OtlpHttpSpanExporter exporter = builder().setJsonEncoding().setCompression("gzip").build();
    exportAndVerify(exporter);
  }

  @Test
  void testExportAsProtobufUncompressed() {
    OtlpHttpSpanExporter exporter = builder().setProtobufEncoding().build();
    exportAndVerify(exporter);
  }

  @Test
  void testExportAsProtobufGzipCompressed() {
    OtlpHttpSpanExporter exporter = builder().setProtobufEncoding().setCompression("gzip").build();
    exportAndVerify(exporter);
  }

  @Test
  void testJsonServerError() {
    otlpHttp.addMockResponse(OtlpHttpDispatcher.errorResponse(Encoding.JSON, 500, "Server error!"));
    OtlpHttpSpanExporter exporter = builder().setJsonEncoding().build();

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export spans. Server responded with code 500. Error message: Server error!");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void testProtobufServerError() {
    otlpHttp.addMockResponse(
        OtlpHttpDispatcher.errorResponse(Encoding.PROTOBUF, 500, "Server error!"));
    OtlpHttpSpanExporter exporter = builder().setProtobufEncoding().build();

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export spans. Server responded with code 500. Error message: Server error!");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void testServerErrorParseError() {
    otlpHttp.addMockResponse(OtlpHttpDispatcher.errorResponse(Encoding.JSON, 500, "Server error!"));
    OtlpHttpSpanExporter exporter = builder().setProtobufEncoding().build();

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export spans. Server responded with code 500. Error message: Unable to extract error message from request:");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  private OtlpHttpSpanExporterBuilder builder() {
    return OtlpHttpSpanExporter.builder()
        .setEndpoint(otlpHttp.endpoint())
        .addHeader("foo", "bar")
        .setTrustedCertificates(otlpHttp.certificatePem().getBytes(StandardCharsets.UTF_8));
  }

  private void exportAndVerify(OtlpHttpSpanExporter otlpHttpSpanExporter) {
    List<SpanData> spans = Collections.singletonList(generateFakeSpan());
    CompletableResultCode resultCode = otlpHttpSpanExporter.export(spans);
    resultCode.join(10, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();

    ExportTraceServiceRequest expectedRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(spans))
            .build();
    List<ExportTraceServiceRequest> requests = otlpHttp.getRequests();
    assertThat(requests.size()).isEqualTo(1);
    assertThat(requests.get(0)).isEqualTo(expectedRequest);
  }

  private static SpanData generateFakeSpan() {
    long duration = TimeUnit.MILLISECONDS.toNanos(900);
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + duration;
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(
            SpanContext.create(
                "00000000000000000000000000abc123",
                "0000000000def456",
                TraceFlags.getDefault(),
                TraceState.getDefault()))
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
}
