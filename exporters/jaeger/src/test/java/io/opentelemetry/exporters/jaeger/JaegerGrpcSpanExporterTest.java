/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.jaeger;

import static io.opentelemetry.exporters.jaeger.TraceProtoUtils.toProtoTraceId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector.PostSpansRequest;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.export.SpanData.TimedEvent;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.net.InetAddress;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

public class JaegerGrpcSpanExporterTest {
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  @Rule public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final CollectorServiceGrpc.CollectorServiceImplBase service =
      mock(
          CollectorServiceGrpc.CollectorServiceImplBase.class,
          delegatesTo(new MockCollectorService()));

  @Test
  public void testExport() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    ArgumentCaptor<PostSpansRequest> requestCaptor =
        ArgumentCaptor.forClass(Collector.PostSpansRequest.class);

    grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start());

    ManagedChannel channel =
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData.Timestamp startTime =
        SpanData.Timestamp.create(startMs / 1000, (int) ((startMs % 1000) * 1000000));
    SpanData.Timestamp endTime =
        SpanData.Timestamp.create(endMs / 1000, (int) ((endMs % 1000) * 1000000));
    SpanData span =
        SpanData.newBuilder()
            .context(
                SpanContext.create(
                    TraceId.fromLowerBase16(TRACE_ID, 0),
                    SpanId.fromLowerBase16(SPAN_ID, 0),
                    TraceFlags.builder().setIsSampled(true).build(),
                    Tracestate.builder().build()))
            .name("GET /api/endpoint")
            .startTimestamp(startTime)
            .endTimestamp(endTime)
            .status(Status.OK)
            .attributes(Collections.<String, AttributeValue>emptyMap())
            .timedEvents(Collections.<TimedEvent>emptyList())
            .resource(Resource.create(Collections.<String, String>emptyMap()))
            .kind(Kind.CONSUMER)
            .links(Collections.<Link>emptyList())
            .parentSpanId(SpanId.getInvalid())
            .build();

    // test
    JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.newBuilder().setServiceName("test").setChannel(channel).build();
    exporter.export(Collections.singletonList(span));

    // verify
    verify(service)
        .postSpans(
            requestCaptor.capture(),
            ArgumentMatchers.<StreamObserver<Collector.PostSpansResponse>>any());

    Model.Batch batch = requestCaptor.getValue().getBatch();
    assertEquals(1, batch.getSpansCount());
    assertEquals("GET /api/endpoint", batch.getSpans(0).getOperationName());
    assertEquals(
        toProtoTraceId(TraceId.fromLowerBase16(TRACE_ID, 0)), batch.getSpans(0).getTraceId());
    assertEquals(
        TraceProtoUtils.toProtoSpanId(SpanId.fromLowerBase16(SPAN_ID, 0)),
        batch.getSpans(0).getSpanId());
    assertEquals("test", batch.getProcess().getServiceName());
    assertEquals(3, batch.getProcess().getTagsCount());

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
