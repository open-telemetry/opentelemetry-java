/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.junit.ZipkinRule;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * Tests which use Zipkin's {@link ZipkinRule} to verify that the {@link ZipkinSpanExporter} can
 * send spans via HTTP to Zipkin's API using supported encodings.
 */
public class ZipkinSpanExporterEndToEndHttpTest {

  private static final String TRACE_ID = "d239036e7d5cec116b562147388b35bf";
  private static final String SPAN_ID = "9cc1e3049173be09";
  private static final String PARENT_SPAN_ID = "8b03ab423da481c5";
  private static final String SPAN_NAME = "Recv.helloworld.Greeter.SayHello";
  private static final long START_EPOCH_NANOS = 1505855794_194009601L;
  private static final long END_EPOCH_NANOS = 1505855799_465726528L;
  private static final long RECEIVED_TIMESTAMP_NANOS = 1505855799_433901068L;
  private static final long SENT_TIMESTAMP_NANOS = 1505855799_459486280L;
  private static final Attributes attributes = Attributes.empty();
  private static final List<Event> annotations =
      ImmutableList.of(
          Event.create(RECEIVED_TIMESTAMP_NANOS, "RECEIVED", Attributes.empty()),
          Event.create(SENT_TIMESTAMP_NANOS, "SENT", Attributes.empty()));

  private static final String ENDPOINT_V1_SPANS = "/api/v1/spans";
  private static final String ENDPOINT_V2_SPANS = "/api/v2/spans";
  private static final String SERVICE_NAME = "myService";
  private static final Endpoint localEndpoint =
      ZipkinSpanExporter.produceLocalEndpoint(SERVICE_NAME);

  @Rule public ZipkinRule zipkin = new ZipkinRule();

  @Test
  public void testExportWithDefaultEncoding() {

    ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder()
            .setEndpoint(zipkin.httpUrl() + ENDPOINT_V2_SPANS)
            .setServiceName(SERVICE_NAME)
            .build();

    exportAndVerify(exporter);
  }

  @Test
  public void testExportAsProtobuf() {

    ZipkinSpanExporter exporter =
        buildZipkinExporter(
            zipkin.httpUrl() + ENDPOINT_V2_SPANS, Encoding.PROTO3, SpanBytesEncoder.PROTO3);
    exportAndVerify(exporter);
  }

  @Test
  public void testExportAsThrift() {

    @SuppressWarnings("deprecation") // we have to use the deprecated thrift encoding to test it
    ZipkinSpanExporter exporter =
        buildZipkinExporter(
            zipkin.httpUrl() + ENDPOINT_V1_SPANS, Encoding.THRIFT, SpanBytesEncoder.THRIFT);
    exportAndVerify(exporter);
  }

  @Test
  public void testExportAsJsonV1() {
    ZipkinSpanExporter exporter =
        buildZipkinExporter(
            zipkin.httpUrl() + ENDPOINT_V1_SPANS, Encoding.JSON, SpanBytesEncoder.JSON_V1);
    exportAndVerify(exporter);
  }

  @Test
  public void testExportFailedAsWrongEncoderUsed() {
    ZipkinSpanExporter zipkinSpanExporter =
        buildZipkinExporter(
            zipkin.httpUrl() + ENDPOINT_V2_SPANS, Encoding.JSON, SpanBytesEncoder.PROTO3);

    SpanData spanData = buildStandardSpan().build();
    CompletableResultCode resultCode = zipkinSpanExporter.export(Collections.singleton(spanData));

    assertThat(resultCode.isSuccess()).isFalse();
    List<Span> zipkinSpans = zipkin.getTrace(TRACE_ID);
    assertThat(zipkinSpans).isNotNull();
    assertThat(zipkinSpans).isEmpty();
  }

  private static ZipkinSpanExporter buildZipkinExporter(
      String endpoint, Encoding encoding, SpanBytesEncoder encoder) {
    return ZipkinSpanExporter.builder()
        .setSender(OkHttpSender.newBuilder().endpoint(endpoint).encoding(encoding).build())
        .setServiceName(SERVICE_NAME)
        .setEncoder(encoder)
        .build();
  }

  /**
   * Exports a span, verify that it was received by Zipkin, and check that the span stored by Zipkin
   * matches what was sent.
   */
  private void exportAndVerify(ZipkinSpanExporter zipkinSpanExporter) {

    SpanData spanData = buildStandardSpan().build();
    CompletableResultCode resultCode = zipkinSpanExporter.export(Collections.singleton(spanData));
    resultCode.join(10, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
    List<Span> zipkinSpans = zipkin.getTrace(TRACE_ID);

    assertThat(zipkinSpans).isNotNull();
    assertThat(zipkinSpans.size()).isEqualTo(1);
    assertThat(zipkinSpans.get(0)).isEqualTo(buildZipkinSpan());
  }

  private static TestSpanData.Builder buildStandardSpan() {
    return TestSpanData.builder()
        .setTraceId(TRACE_ID)
        .setSpanId(SPAN_ID)
        .setParentSpanId(PARENT_SPAN_ID)
        .setSampled(true)
        .setStatus(SpanData.Status.ok())
        .setKind(Kind.SERVER)
        .setHasRemoteParent(true)
        .setName(SPAN_NAME)
        .setStartEpochNanos(START_EPOCH_NANOS)
        .setAttributes(attributes)
        .setTotalAttributeCount(attributes.size())
        .setEvents(annotations)
        .setLinks(Collections.emptyList())
        .setEndEpochNanos(END_EPOCH_NANOS)
        .setHasEnded(true);
  }

  private static Span buildZipkinSpan() {
    return Span.newBuilder()
        .traceId(TRACE_ID)
        .parentId(PARENT_SPAN_ID)
        .id(SPAN_ID)
        .kind(Span.Kind.SERVER)
        .name(SPAN_NAME)
        .timestamp(START_EPOCH_NANOS / 1000)
        .duration((END_EPOCH_NANOS / 1000) - (START_EPOCH_NANOS / 1000))
        .localEndpoint(localEndpoint)
        .addAnnotation(RECEIVED_TIMESTAMP_NANOS / 1000, "RECEIVED")
        .addAnnotation(SENT_TIMESTAMP_NANOS / 1000, "SENT")
        .putTag(ZipkinSpanExporter.OTEL_STATUS_CODE, "OK")
        .build();
  }
}
