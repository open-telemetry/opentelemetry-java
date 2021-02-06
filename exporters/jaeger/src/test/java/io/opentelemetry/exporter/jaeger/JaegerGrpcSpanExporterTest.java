/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Closer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporter.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

class JaegerGrpcSpanExporterTest {
  private static final BaseEncoding hex = BaseEncoding.base16().lowerCase();
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";
  private static final String SPAN_ID_2 = "0000000000aef789";

  private final Closer closer = Closer.create();
  private ArgumentCaptor<Collector.PostSpansRequest> requestCaptor;
  private JaegerGrpcSpanExporter exporter;

  @BeforeEach
  public void beforeEach() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    requestCaptor = ArgumentCaptor.forClass(Collector.PostSpansRequest.class);

    Server server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start();
    closer.register(server::shutdownNow);

    ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    exporter = JaegerGrpcSpanExporter.builder().setChannel(channel).build();
  }

  @AfterEach
  void tearDown() throws Exception {
    closer.close();
  }

  private final CollectorServiceGrpc.CollectorServiceImplBase service =
      mock(
          CollectorServiceGrpc.CollectorServiceImplBase.class,
          delegatesTo(new MockCollectorService()));

  @Test
  void testExport() throws Exception {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(
                SpanContext.create(
                    TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()))
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(StatusData.ok())
            .setKind(SpanKind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        ResourceAttributes.SERVICE_NAME,
                        "myServiceName",
                        AttributeKey.stringKey("resource-attr-key"),
                        "resource-attr-value")))
            .build();

    // test
    CompletableResultCode result = exporter.export(Collections.singletonList(span));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    verify(service).postSpans(requestCaptor.capture(), ArgumentMatchers.any());

    Model.Batch batch = requestCaptor.getValue().getBatch();
    assertThat(batch.getSpans(0).getOperationName()).isEqualTo("GET /api/endpoint");
    assertThat(hex.encode(batch.getSpans(0).getSpanId().toByteArray())).isEqualTo(SPAN_ID);

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "resource-attr-key")
                .orElseThrow(() -> new AssertionError("resource-attr-key not found"))
                .getVStr())
        .isEqualTo("resource-attr-value");

    verifyBatch(batch);
    assertThat(batch.getProcess().getServiceName()).isEqualTo("myServiceName");
  }

  @Test
  void testExportMultipleResources() throws Exception {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(
                SpanContext.create(
                    TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()))
            .setName("GET /api/endpoint/1")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(StatusData.ok())
            .setKind(SpanKind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        ResourceAttributes.SERVICE_NAME,
                        "myServiceName1",
                        AttributeKey.stringKey("resource-attr-key-1"),
                        "resource-attr-value-1")))
            .build();

    SpanData span2 =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(
                SpanContext.create(
                    TRACE_ID, SPAN_ID_2, TraceFlags.getSampled(), TraceState.getDefault()))
            .setName("GET /api/endpoint/2")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(StatusData.ok())
            .setKind(SpanKind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        ResourceAttributes.SERVICE_NAME,
                        "myServiceName2",
                        AttributeKey.stringKey("resource-attr-key-2"),
                        "resource-attr-value-2")))
            .build();

    // test
    CompletableResultCode result = exporter.export(Lists.newArrayList(span, span2));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    verify(service, times(2)).postSpans(requestCaptor.capture(), ArgumentMatchers.any());

    List<Collector.PostSpansRequest> requests = requestCaptor.getAllValues();
    assertThat(requests).hasSize(2);
    for (Collector.PostSpansRequest request : requests) {
      Model.Batch batch = request.getBatch();

      verifyBatch(batch);

      Optional<Model.KeyValue> processTag =
          getTagValue(batch.getProcess().getTagsList(), "resource-attr-key-1");
      Optional<Model.KeyValue> processTag2 =
          getTagValue(batch.getProcess().getTagsList(), "resource-attr-key-2");
      if (processTag.isPresent()) {
        assertThat(processTag2.isPresent()).isFalse();
        assertThat(batch.getSpans(0).getOperationName()).isEqualTo("GET /api/endpoint/1");
        assertThat(hex.encode(batch.getSpans(0).getSpanId().toByteArray())).isEqualTo(SPAN_ID);
        assertThat(processTag.get().getVStr()).isEqualTo("resource-attr-value-1");
        assertThat(batch.getProcess().getServiceName()).isEqualTo("myServiceName1");
      } else if (processTag2.isPresent()) {
        assertThat(batch.getSpans(0).getOperationName()).isEqualTo("GET /api/endpoint/2");
        assertThat(hex.encode(batch.getSpans(0).getSpanId().toByteArray())).isEqualTo(SPAN_ID_2);
        assertThat(processTag2.get().getVStr()).isEqualTo("resource-attr-value-2");
        assertThat(batch.getProcess().getServiceName()).isEqualTo("myServiceName2");
      } else {
        fail("No process tag resource-attr-key-1 or resource-attr-key-2");
      }
    }
  }

  private static void verifyBatch(Model.Batch batch) throws Exception {
    assertThat(batch.getSpansCount()).isEqualTo(1);
    assertThat(hex.encode(batch.getSpans(0).getTraceId().toByteArray())).isEqualTo(TRACE_ID);
    assertThat(batch.getProcess().getTagsCount()).isEqualTo(5);

    assertThat(
            getSpanTagValue(batch.getSpans(0), "otel.library.name")
                .orElseThrow(() -> new AssertionError("otel.library.name not found"))
                .getVStr())
        .isEqualTo("io.opentelemetry.auto");

    assertThat(
            getSpanTagValue(batch.getSpans(0), "otel.library.version")
                .orElseThrow(() -> new AssertionError("otel.library.version not found"))
                .getVStr())
        .isEqualTo("1.0.0");

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "ip")
                .orElseThrow(() -> new AssertionError("ip not found"))
                .getVStr())
        .isEqualTo(InetAddress.getLocalHost().getHostAddress());

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "hostname")
                .orElseThrow(() -> new AssertionError("hostname not found"))
                .getVStr())
        .isEqualTo(InetAddress.getLocalHost().getHostName());

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "jaeger.version")
                .orElseThrow(() -> new AssertionError("jaeger.version not found"))
                .getVStr())
        .isEqualTo("opentelemetry-java");
  }

  private static Optional<Model.KeyValue> getSpanTagValue(Model.Span span, String tagKey) {
    return getTagValue(span.getTagsList(), tagKey);
  }

  private static Optional<Model.KeyValue> getTagValue(List<Model.KeyValue> tags, String tagKey) {
    return tags.stream().filter(kv -> kv.getKey().equals(tagKey)).findFirst();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");
  }

  static class MockCollectorService extends CollectorServiceGrpc.CollectorServiceImplBase {
    @Override
    public void postSpans(
        Collector.PostSpansRequest request,
        StreamObserver<Collector.PostSpansResponse> responseObserver) {
      responseObserver.onNext(Collector.PostSpansResponse.newBuilder().build());
      responseObserver.onCompleted();
    }
  }
}
