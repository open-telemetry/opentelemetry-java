/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.io.Closer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector.PostSpansRequest;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model.KeyValue;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model.Span;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.extensions.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import io.opentelemetry.trace.Span.Kind;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class JaegerGrpcSpanExporterTest {
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  private final Closer closer = Closer.create();

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
    String serverName = InProcessServerBuilder.generateName();
    ArgumentCaptor<PostSpansRequest> requestCaptor =
        ArgumentCaptor.forClass(Collector.PostSpansRequest.class);

    Server server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start();
    closer.register(server::shutdownNow);

    ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    closer.register(channel::shutdownNow);

    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData span =
        TestSpanData.newBuilder()
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
            .build();

    // test
    JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.newBuilder().setServiceName("test").setChannel(channel).build();
    exporter.export(Collections.singletonList(span));

    // verify
    verify(service).postSpans(requestCaptor.capture(), ArgumentMatchers.any());

    Model.Batch batch = requestCaptor.getValue().getBatch();
    assertEquals(1, batch.getSpansCount());
    assertEquals("GET /api/endpoint", batch.getSpans(0).getOperationName());
    assertEquals(TraceProtoUtils.toProtoTraceId(TRACE_ID), batch.getSpans(0).getTraceId());
    assertEquals(TraceProtoUtils.toProtoSpanId(SPAN_ID), batch.getSpans(0).getSpanId());
    assertEquals("test", batch.getProcess().getServiceName());
    assertEquals(3, batch.getProcess().getTagsCount());

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

    boolean foundClientTag = false;
    boolean foundHostname = false;
    boolean foundIp = false;
    for (Model.KeyValue kv : batch.getProcess().getTagsList()) {
      if (kv.getKey().equals("jaeger.version")) {
        foundClientTag = true;
        assertEquals("opentelemetry-java", kv.getVStr());
      }

      if (kv.getKey().equals("ip")) {
        foundIp = true;
        assertEquals(InetAddress.getLocalHost().getHostAddress(), kv.getVStr());
      }

      if (kv.getKey().equals("hostname")) {
        foundHostname = true;
        assertEquals(InetAddress.getLocalHost().getHostName(), kv.getVStr());
      }
    }
    assertTrue("a client tag should have been present", foundClientTag);
    assertTrue("an ip tag should have been present", foundIp);
    assertTrue("a hostname tag should have been present", foundHostname);
  }

  private static Optional<KeyValue> getSpanTagValue(Span span, String tagKey) {
    return span.getTagsList().stream().filter(kv -> kv.getKey().equals(tagKey)).findFirst();
  }

  @Test
  void configTest() {
    Map<String, String> options = new HashMap<>();
    String serviceName = "myGreatService";
    String endpoint = "127.0.0.1:9090";
    options.put("otel.exporter.jaeger.service.name", serviceName);
    options.put("otel.exporter.jaeger.endpoint", endpoint);
    JaegerGrpcSpanExporter.Builder config = JaegerGrpcSpanExporter.newBuilder();
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
