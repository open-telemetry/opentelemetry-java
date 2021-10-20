/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.v1.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.codec.SpanBytesDecoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Testcontainers(disabledWithoutDocker = true)
class ZipkinSpanExporterEndToEndHttpTest {
  private static final WebClient client = WebClient.of();

  private static final int ZIPKIN_API_PORT = 9411;

  private static final String SPAN_ID = "9cc1e3049173be09";
  private static final String PARENT_SPAN_ID = "8b03ab423da481c5";
  private static final String SPAN_NAME = "Recv.helloworld.Greeter.SayHello";
  private static final long START_EPOCH_NANOS = 1505855794_194009601L;
  private static final long END_EPOCH_NANOS = 1505855799_465726528L;
  private static final long RECEIVED_TIMESTAMP_NANOS = 1505855799_433901068L;
  private static final long SENT_TIMESTAMP_NANOS = 1505855799_459486280L;
  private static final Attributes attributes = Attributes.empty();
  private static final List<EventData> annotations =
      Collections.unmodifiableList(
          Arrays.asList(
              EventData.create(RECEIVED_TIMESTAMP_NANOS, "RECEIVED", Attributes.empty()),
              EventData.create(SENT_TIMESTAMP_NANOS, "SENT", Attributes.empty())));

  private static final String ENDPOINT_V1_SPANS = "/api/v1/spans";
  private static final String ENDPOINT_V2_SPANS = "/api/v2/spans";
  private static final String SERVICE_NAME = "myService";

  @Container
  public static GenericContainer<?> zipkinContainer =
      new GenericContainer<>("ghcr.io/openzipkin/zipkin:2.23")
          .withExposedPorts(ZIPKIN_API_PORT)
          .waitingFor(Wait.forHttp("/health").forPort(ZIPKIN_API_PORT));

  @Test
  void testExportWithDefaultEncoding() {
    ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder().setEndpoint(zipkinUrl(ENDPOINT_V2_SPANS)).build();

    exportAndVerify(exporter);
  }

  @Test
  void testExportAsProtobuf() {
    ZipkinSpanExporter exporter =
        buildZipkinExporter(zipkinUrl(ENDPOINT_V2_SPANS), Encoding.PROTO3, SpanBytesEncoder.PROTO3);
    exportAndVerify(exporter);
  }

  @Test
  void testExportAsThrift() {
    @SuppressWarnings("deprecation") // we have to use the deprecated thrift encoding to test it
    ZipkinSpanExporter exporter =
        buildZipkinExporter(zipkinUrl(ENDPOINT_V1_SPANS), Encoding.THRIFT, SpanBytesEncoder.THRIFT);
    exportAndVerify(exporter);
  }

  @Test
  void testExportAsJsonV1() {
    ZipkinSpanExporter exporter =
        buildZipkinExporter(zipkinUrl(ENDPOINT_V1_SPANS), Encoding.JSON, SpanBytesEncoder.JSON_V1);
    exportAndVerify(exporter);
  }

  @Test
  void testExportFailedAsWrongEncoderUsed() {
    ZipkinSpanExporter zipkinSpanExporter =
        buildZipkinExporter(zipkinUrl(ENDPOINT_V2_SPANS), Encoding.JSON, SpanBytesEncoder.PROTO3);

    String traceId = IdGenerator.random().generateTraceId();
    SpanData spanData = buildStandardSpan(traceId).build();
    CompletableResultCode resultCode = zipkinSpanExporter.export(Collections.singleton(spanData));

    assertThat(resultCode.isSuccess()).isFalse();
    List<Span> zipkinSpans = getTrace(traceId);
    assertThat(zipkinSpans).isEmpty();
  }

  private static ZipkinSpanExporter buildZipkinExporter(
      String endpoint, Encoding encoding, SpanBytesEncoder encoder) {
    return ZipkinSpanExporter.builder()
        .setSender(OkHttpSender.newBuilder().endpoint(endpoint).encoding(encoding).build())
        .setEncoder(encoder)
        .build();
  }

  /**
   * Exports a span, verify that it was received by Zipkin, and check that the span stored by Zipkin
   * matches what was sent.
   */
  private static void exportAndVerify(ZipkinSpanExporter zipkinSpanExporter) {
    String traceId = IdGenerator.random().generateTraceId();
    SpanData spanData = buildStandardSpan(traceId).build();
    CompletableResultCode resultCode = zipkinSpanExporter.export(Collections.singleton(spanData));
    resultCode.join(10, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
    List<Span> zipkinSpans = getTrace(traceId);

    assertThat(zipkinSpans).isNotNull();
    assertThat(zipkinSpans.size()).isEqualTo(1);
    assertThat(zipkinSpans.get(0))
        .isEqualTo(buildZipkinSpan(zipkinSpanExporter.getLocalAddressForTest(), traceId));
  }

  private static TestSpanData.Builder buildStandardSpan(String traceId) {
    return TestSpanData.builder()
        .setSpanContext(
            SpanContext.create(traceId, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()))
        .setParentSpanContext(
            SpanContext.create(
                traceId, PARENT_SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setStatus(StatusData.ok())
        .setKind(SpanKind.SERVER)
        .setName(SPAN_NAME)
        .setStartEpochNanos(START_EPOCH_NANOS)
        .setAttributes(attributes)
        .setTotalAttributeCount(attributes.size())
        .setTotalRecordedEvents(annotations.size())
        .setEvents(annotations)
        .setLinks(Collections.emptyList())
        .setEndEpochNanos(END_EPOCH_NANOS)
        .setHasEnded(true)
        .setResource(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, SERVICE_NAME)));
  }

  private static Span buildZipkinSpan(InetAddress localAddress, String traceId) {
    return Span.newBuilder()
        .traceId(traceId)
        .parentId(PARENT_SPAN_ID)
        .id(SPAN_ID)
        .kind(Span.Kind.SERVER)
        .name(SPAN_NAME)
        .timestamp(START_EPOCH_NANOS / 1000)
        .duration((END_EPOCH_NANOS / 1000) - (START_EPOCH_NANOS / 1000))
        .localEndpoint(Endpoint.newBuilder().serviceName(SERVICE_NAME).ip(localAddress).build())
        .addAnnotation(RECEIVED_TIMESTAMP_NANOS / 1000, "RECEIVED")
        .addAnnotation(SENT_TIMESTAMP_NANOS / 1000, "SENT")
        .putTag(ZipkinSpanExporter.OTEL_STATUS_CODE, "OK")
        .build();
  }

  private static List<Span> getTrace(String traceId) {
    AggregatedHttpResponse response =
        client.get(zipkinUrl("/api/v2/trace/" + traceId)).aggregate().join();
    if (response.status().equals(HttpStatus.NOT_FOUND)) {
      return Collections.emptyList();
    }
    return SpanBytesDecoder.JSON_V2.decodeList(response.content().array());
  }

  private static String zipkinUrl(String endpoint) {
    return "http://localhost:" + zipkinContainer.getMappedPort(ZIPKIN_API_PORT) + endpoint;
  }
}
