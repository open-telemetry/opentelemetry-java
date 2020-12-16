/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.io.Closer;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpGrpcSpanExporterTest {

  @RegisterExtension
  public static ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
          sb.service(
              GrpcService.builder()
                  .addService(
                      new TraceServiceGrpc.TraceServiceImplBase() {
                        @Override
                        public void export(
                            ExportTraceServiceRequest request,
                            StreamObserver<ExportTraceServiceResponse> responseObserver) {
                          RequestHeaders headers =
                              ServiceRequestContext.current().request().headers();
                          if (headers.get("key").equals("value")
                              && headers.get("key2").equals("value2=")
                              && headers.get("key3").equals("val=ue3")
                              && headers.get("key4").equals("value4")
                              && !headers.contains("key5")) {
                            responseObserver.onNext(
                                ExportTraceServiceResponse.getDefaultInstance());
                            responseObserver.onCompleted();
                          } else {
                            responseObserver.onError(new AssertionError("Invalid metadata"));
                          }
                        }
                      })
                  .build());
        }
      };

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
    String endpoint = "localhost:" + server.httpPort();
    options.put("otel.exporter.otlp.span.timeout", "5124");
    options.put("otel.exporter.otlp.span.endpoint", endpoint);
    options.put("otel.exporter.otlp.span.insecure", "true");
    options.put(
        "otel.exporter.otlp.span.headers",
        "key=value;key2=value2=;key3=val=ue3; key4 = value4 ;key5= ");
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .fromConfigMap(options, OtlpGrpcMetricExporterTest.ConfigBuilderTest.getNaming())
            .build();

    assertThat(exporter.getDeadlineMs()).isEqualTo(5124);
    assertThat(
            exporter
                .export(
                    Arrays.asList(
                        TestSpanData.builder()
                            .setTraceId(TraceId.getInvalid())
                            .setSpanId(SpanId.getInvalid())
                            .setName("name")
                            .setKind(Kind.CLIENT)
                            .setStartEpochNanos(1)
                            .setEndEpochNanos(2)
                            .setStatus(SpanData.Status.ok())
                            .setHasEnded(true)
                            .build()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
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
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
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
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
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
    int deadlineMs = 1500;
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setChannel(inProcessChannel)
            .setDeadlineMs(deadlineMs)
            .build();

    try {
      TimeUnit.MILLISECONDS.sleep(2000);
      CompletableResultCode result = exporter.export(Collections.singletonList(generateFakeSpan()));
      await().untilAsserted(() -> assertThat(result.isSuccess()).isTrue());
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_AfterShutdown() {
    SpanData span = generateFakeSpan();
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    exporter.shutdown();
    // TODO: This probably should not be retryable because we never restart the channel.
    assertThat(exporter.export(Collections.singletonList(span)).isSuccess()).isFalse();
  }

  @Test
  void testExport_Cancelled() {
    fakeCollector.setReturnedStatus(Status.CANCELLED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_DeadlineExceeded() {
    fakeCollector.setReturnedStatus(Status.DEADLINE_EXCEEDED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_ResourceExhausted() {
    fakeCollector.setReturnedStatus(Status.RESOURCE_EXHAUSTED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_OutOfRange() {
    fakeCollector.setReturnedStatus(Status.OUT_OF_RANGE);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_Unavailable() {
    fakeCollector.setReturnedStatus(Status.UNAVAILABLE);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_DataLoss() {
    fakeCollector.setReturnedStatus(Status.DATA_LOSS);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_PermissionDenied() {
    fakeCollector.setReturnedStatus(Status.PERMISSION_DENIED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
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
    return TestSpanData.builder()
        .setHasEnded(true)
        .setTraceId(TRACE_ID)
        .setSpanId(SPAN_ID)
        .setName("GET /api/endpoint")
        .setStartEpochNanos(startNs)
        .setEndEpochNanos(endNs)
        .setStatus(SpanData.Status.ok())
        .setKind(Kind.SERVER)
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }

  private static final class FakeCollector extends TraceServiceGrpc.TraceServiceImplBase {
    private final List<ResourceSpans> receivedSpans = new ArrayList<>();
    private Status returnedStatus = Status.OK;

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

    void setReturnedStatus(Status returnedStatus) {
      this.returnedStatus = returnedStatus;
    }
  }
}
