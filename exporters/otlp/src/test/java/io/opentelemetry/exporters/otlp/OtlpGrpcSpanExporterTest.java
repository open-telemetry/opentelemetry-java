/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.exporters.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Closer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status.Code;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporterTest.ConfigBuilderTest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OtlpGrpcSpanExporterTest {
  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  private final FakeCollector fakeCollector = new FakeCollector();
  private final String serverName = InProcessServerBuilder.generateName();
  private final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();

  private final Closer closer = Closer.create();

  @Test
  void configTest() {
    Map<String, String> options = new HashMap<>();
    options.put("otel.otlp.span.timeout", "12");
    options.put("otel.otlp.endpoint", "http://localhost:6553");
    options.put("otel.otlp.use.tls", "true");
    options.put("otel.otlp.metadata", "key=value;key2=value2=;key3=val=ue3; key4 = value4 ;key5= ");
    OtlpGrpcSpanExporter.Builder config = OtlpGrpcSpanExporter.newBuilder();
    OtlpGrpcSpanExporter.Builder spy = Mockito.spy(config);
    spy.fromConfigMap(options, ConfigBuilderTest.getNaming());
    Mockito.verify(spy).setDeadlineMs(12);
    Mockito.verify(spy).setEndpoint("http://localhost:6553");
    Mockito.verify(spy).setUseTls(true);
    Mockito.verify(spy).addHeader("key", "value");
    Mockito.verify(spy).addHeader("key2", "value2=");
    Mockito.verify(spy).addHeader("key3", "val=ue3");
    Mockito.verify(spy).addHeader("key4", "value4");
    Mockito.verify(spy, Mockito.never()).addHeader("key5", "");
  }

  @BeforeEach
  public void setup() throws IOException {
    Server server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(fakeCollector)
            .build()
            .start();
    closer.register(server::shutdownNow);
    closer.register(inProcessChannel::shutdownNow);
  }

  @AfterEach
  void tearDown() throws Exception {
    closer.close();
  }

  @Test
  void testExport() {
    SpanData span = generateFakeSpan();
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(span)).isSuccess()).isTrue();
      assertThat(fakeCollector.getReceivedSpans())
          .isEqualTo(SpanAdapter.toProtoResourceSpans(Collections.singletonList(span)));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_MultipleSpans() {
    List<SpanData> spans = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      spans.add(generateFakeSpan());
    }
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(spans).isSuccess()).isTrue();
      assertThat(fakeCollector.getReceivedSpans())
          .isEqualTo(SpanAdapter.toProtoResourceSpans(spans));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_DeadlineSetPerExport() throws InterruptedException {
    int deadlineMs = 500;
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder()
            .setChannel(inProcessChannel)
            .setDeadlineMs(deadlineMs)
            .build();

    try {
      TimeUnit.MILLISECONDS.sleep(2 * deadlineMs);
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isTrue();

      TimeUnit.MILLISECONDS.sleep(2 * deadlineMs);
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_AfterShutdown() {
    SpanData span = generateFakeSpan();
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    exporter.shutdown();
    // TODO: This probably should not be retryable because we never restart the channel.
    assertThat(exporter.export(Collections.singletonList(span)).isSuccess()).isFalse();
  }

  @Test
  void testExport_Cancelled() {
    fakeCollector.setReturnedStatus(io.grpc.Status.CANCELLED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_DeadlineExceeded() {
    fakeCollector.setReturnedStatus(io.grpc.Status.DEADLINE_EXCEEDED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_ResourceExhausted() {
    fakeCollector.setReturnedStatus(io.grpc.Status.RESOURCE_EXHAUSTED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_OutOfRange() {
    fakeCollector.setReturnedStatus(io.grpc.Status.OUT_OF_RANGE);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_Unavailable() {
    fakeCollector.setReturnedStatus(io.grpc.Status.UNAVAILABLE);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_DataLoss() {
    fakeCollector.setReturnedStatus(io.grpc.Status.DATA_LOSS);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_PermissionDenied() {
    fakeCollector.setReturnedStatus(io.grpc.Status.PERMISSION_DENIED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  private static SpanData generateFakeSpan() {
    long duration = TimeUnit.MILLISECONDS.toNanos(900);
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + duration;
    return TestSpanData.newBuilder()
        .setHasEnded(true)
        .setTraceId(TraceId.bytesFromLowerBase16(TRACE_ID, 0))
        .setSpanId(SpanId.bytesFromLowerBase16(SPAN_ID, 0))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(startNs)
        .setEndEpochNanos(endNs)
        .setStatus(Status.OK)
        .setKind(Kind.SERVER)
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }

  private static final class FakeCollector extends TraceServiceGrpc.TraceServiceImplBase {
    private final List<ResourceSpans> receivedSpans = new ArrayList<>();
    private io.grpc.Status returnedStatus = io.grpc.Status.OK;

    @Override
    public void export(
        ExportTraceServiceRequest request,
        StreamObserver<ExportTraceServiceResponse> responseObserver) {
      receivedSpans.addAll(request.getResourceSpansList());
      responseObserver.onNext(ExportTraceServiceResponse.newBuilder().build());
      if (!returnedStatus.isOk()) {
        if (returnedStatus.getCode() == Code.DEADLINE_EXCEEDED) {
          // Do not call onCompleted to simulate a deadline exceeded.
          return;
        }
        responseObserver.onError(returnedStatus.asRuntimeException());
        return;
      }
      responseObserver.onCompleted();
    }

    List<ResourceSpans> getReceivedSpans() {
      return receivedSpans;
    }

    void setReturnedStatus(io.grpc.Status returnedStatus) {
      this.returnedStatus = returnedStatus;
    }
  }
}
