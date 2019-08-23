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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.proto.trace.v1.Span;
import java.net.InetAddress;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

public class JaegerGrpcSpanExporterTest {

  @Rule public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final CollectorServiceGrpc.CollectorServiceImplBase service =
      mock(
          CollectorServiceGrpc.CollectorServiceImplBase.class,
          delegatesTo(new MockCollectorService()));

  @Test
  public void testExport() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    ArgumentCaptor<Collector.PostSpansRequest> requestCaptor =
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
    Timestamp startTime =
        Timestamp.newBuilder()
            .setSeconds(startMs / 1000)
            .setNanos((int) ((startMs % 1000) * 1000000))
            .build();
    Timestamp endTime =
        Timestamp.newBuilder()
            .setSeconds(endMs / 1000)
            .setNanos((int) ((endMs % 1000) * 1000000))
            .build();
    Span span =
        Span.newBuilder()
            .setTraceId(ByteString.copyFromUtf8("abc123"))
            .setSpanId(ByteString.copyFromUtf8("def456"))
            .setName("GET /api/endpoint")
            .setStartTime(startTime)
            .setEndTime(endTime)
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
    assertEquals("abc123", batch.getSpans(0).getTraceId().toStringUtf8());
    assertEquals("def456", batch.getSpans(0).getSpanId().toStringUtf8());
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
