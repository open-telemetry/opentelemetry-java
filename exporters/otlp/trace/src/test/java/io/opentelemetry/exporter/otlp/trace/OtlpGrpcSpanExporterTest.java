/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.google.common.io.Closer;
import io.github.netmikey.logunit.api.LogCapturer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.internal.SpanAdapter;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpGrpcSpanExporterTest {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  private final FakeCollector fakeCollector = new FakeCollector();
  private final String serverName = InProcessServerBuilder.generateName();
  private final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();

  private final Closer closer = Closer.create();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpGrpcSpanExporter.class);

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
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpGrpcSpanExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");
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
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setChannel(inProcessChannel)
            .setTimeout(Duration.ofMillis(1500))
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
    LoggingEvent log =
        logs.assertContains(
            "Failed to export spans. Server is UNAVAILABLE. "
                + "Make sure your collector is running and reachable from this network.");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void testExport_Unimplemented() {
    fakeCollector.setReturnedStatus(Status.UNIMPLEMENTED);
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeSpan())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
    LoggingEvent log =
        logs.assertContains(
            "Failed to export spans. Server responded with UNIMPLEMENTED. "
                + "This usually means that your collector is not configured with an otlp "
                + "receiver in the \"pipelines\" section of the configuration. "
                + "Full error message: UNIMPLEMENTED");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
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
        .setSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(startNs)
        .setEndEpochNanos(endNs)
        .setStatus(StatusData.ok())
        .setKind(SpanKind.SERVER)
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
