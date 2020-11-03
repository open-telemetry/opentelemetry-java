/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector.PostSpansRequest;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model.KeyValue;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model.Span;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.extensions.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class JaegerGrpcSpanExporterTest {
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";
  private static final String SPAN_ID_2 = "0000000000aef789";

  private final Closer closer = Closer.create();
  private ArgumentCaptor<PostSpansRequest> requestCaptor;
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
    exporter = JaegerGrpcSpanExporter.builder().setServiceName("test").setChannel(channel).build();
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
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(Status.ok())
            .setKind(Kind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("resource-attr-key"), "resource-attr-value")))
            .build();

    // test
    CompletableResultCode result = exporter.export(Collections.singletonList(span));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    verify(service).postSpans(requestCaptor.capture(), ArgumentMatchers.any());

    Model.Batch batch = requestCaptor.getValue().getBatch();
    assertEquals("GET /api/endpoint", batch.getSpans(0).getOperationName());
    assertEquals(TraceProtoUtils.toProtoSpanId(SPAN_ID), batch.getSpans(0).getSpanId());

    assertEquals(
        "resource-attr-value",
        getTagValue(batch.getProcess().getTagsList(), "resource-attr-key")
            .orElseThrow(() -> new AssertionError("resource-attr-key not found"))
            .getVStr());

    verifyBatch(batch);
  }

  @Test
  void testExportMultipleResources() throws Exception {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint/1")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(Status.ok())
            .setKind(Kind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("resource-attr-key-1"), "resource-attr-value-1")))
            .build();

    SpanData span2 =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID_2)
            .setName("GET /api/endpoint/2")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(Status.ok())
            .setKind(Kind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("resource-attr-key-2"), "resource-attr-value-2")))
            .build();

    // test
    CompletableResultCode result = exporter.export(Lists.newArrayList(span, span2));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    verify(service, times(2)).postSpans(requestCaptor.capture(), ArgumentMatchers.any());

    List<PostSpansRequest> requests = requestCaptor.getAllValues();
    assertEquals(2, requests.size());
    for (PostSpansRequest request : requests) {
      Model.Batch batch = request.getBatch();

      verifyBatch(batch);

      Optional<KeyValue> processTag =
          getTagValue(batch.getProcess().getTagsList(), "resource-attr-key-1");
      Optional<KeyValue> processTag2 =
          getTagValue(batch.getProcess().getTagsList(), "resource-attr-key-2");
      if (processTag.isPresent()) {
        assertFalse(processTag2.isPresent());
        assertEquals("GET /api/endpoint/1", batch.getSpans(0).getOperationName());
        assertEquals(TraceProtoUtils.toProtoSpanId(SPAN_ID), batch.getSpans(0).getSpanId());
        assertEquals("resource-attr-value-1", processTag.get().getVStr());
      } else if (processTag2.isPresent()) {
        assertEquals("GET /api/endpoint/2", batch.getSpans(0).getOperationName());
        assertEquals(TraceProtoUtils.toProtoSpanId(SPAN_ID_2), batch.getSpans(0).getSpanId());
        assertEquals("resource-attr-value-2", processTag2.get().getVStr());
      } else {
        fail("No process tag resource-attr-key-1 or resource-attr-key-2");
      }
    }
  }

  private static void verifyBatch(Model.Batch batch) throws Exception {
    assertEquals(1, batch.getSpansCount());
    assertEquals(TraceProtoUtils.toProtoTraceId(TRACE_ID), batch.getSpans(0).getTraceId());
    assertEquals("test", batch.getProcess().getServiceName());
    assertEquals(4, batch.getProcess().getTagsCount());

    assertEquals(
        "io.opentelemetry.auto",
        getSpanTagValue(batch.getSpans(0), "otel.library.name")
            .orElseThrow(() -> new AssertionError("otel.library.name not found"))
            .getVStr());

    assertEquals(
        "1.0.0",
        getSpanTagValue(batch.getSpans(0), "otel.library.version")
            .orElseThrow(() -> new AssertionError("otel.library.version not found"))
            .getVStr());

    assertEquals(
        InetAddress.getLocalHost().getHostAddress(),
        getTagValue(batch.getProcess().getTagsList(), "ip")
            .orElseThrow(() -> new AssertionError("ip not found"))
            .getVStr());

    assertEquals(
        InetAddress.getLocalHost().getHostName(),
        getTagValue(batch.getProcess().getTagsList(), "hostname")
            .orElseThrow(() -> new AssertionError("hostname not found"))
            .getVStr());

    assertEquals(
        "opentelemetry-java",
        getTagValue(batch.getProcess().getTagsList(), "jaeger.version")
            .orElseThrow(() -> new AssertionError("jaeger.version not found"))
            .getVStr());
  }

  private static Optional<KeyValue> getSpanTagValue(Span span, String tagKey) {
    return getTagValue(span.getTagsList(), tagKey);
  }

  private static Optional<KeyValue> getTagValue(List<KeyValue> tags, String tagKey) {
    return tags.stream().filter(kv -> kv.getKey().equals(tagKey)).findFirst();
  }

  @Test
  void configTest() {
    Map<String, String> options = new HashMap<>();
    String serviceName = "myGreatService";
    String endpoint = "127.0.0.1:9090";
    options.put("otel.exporter.jaeger.service.name", serviceName);
    options.put("otel.exporter.jaeger.endpoint", endpoint);
    JaegerGrpcSpanExporter.Builder config = JaegerGrpcSpanExporter.builder();
    JaegerGrpcSpanExporter.Builder spy = Mockito.spy(config);
    spy.fromConfigMap(options, ConfigBuilderTest.getNaming()).build();
    Mockito.verify(spy).setServiceName(serviceName);
    Mockito.verify(spy).setEndpoint(endpoint);
  }

  abstract static class ConfigBuilderTest extends ConfigBuilder<ConfigBuilderTest> {
    public static NamingConvention getNaming() {
      return NamingConvention.DOT;
    }
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
