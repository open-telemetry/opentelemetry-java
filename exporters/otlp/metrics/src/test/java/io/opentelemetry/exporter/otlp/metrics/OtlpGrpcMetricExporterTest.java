/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Closer;
import io.github.netmikey.logunit.api.LogCapturer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporter;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpGrpcMetricExporterTest {

  private final FakeCollector fakeCollector = new FakeCollector();
  private final String serverName = InProcessServerBuilder.generateName();
  private final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();

  private final Closer closer = Closer.create();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DefaultGrpcExporter.class);

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
  void validConfig() {
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setCompression("gzip"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setCompression("none"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> OtlpGrpcMetricExporter.builder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpGrpcMetricExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost")
        .hasCauseInstanceOf(URISyntaxException.class);
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  @Test
  void testExport() {
    MetricData metric = generateFakeMetric();
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(metric)).isSuccess()).isTrue();
      assertThat(fakeCollector.getReceivedMetrics())
          .isEqualTo(toResourceMetrics(Collections.singletonList(metric)));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_MultipleMetrics() {
    List<MetricData> metrics = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      metrics.add(generateFakeMetric());
    }
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(metrics).isSuccess()).isTrue();
      assertThat(fakeCollector.getReceivedMetrics()).isEqualTo(toResourceMetrics(metrics));
    } finally {
      exporter.shutdown();
    }
  }

  private static List<ResourceMetrics> toResourceMetrics(List<MetricData> metrics) {
    return Arrays.stream(ResourceMetricsMarshaler.create(metrics))
        .map(
            marshaler -> {
              ByteArrayOutputStream bos = new ByteArrayOutputStream();
              try {
                marshaler.writeBinaryTo(bos);
                return ResourceMetrics.parseFrom(bos.toByteArray());
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            })
        .collect(Collectors.toList());
  }

  @Test
  void testExport_DeadlineSetPerExport() throws Exception {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setChannel(inProcessChannel)
            .setTimeout(Duration.ofMillis(1500))
            .build();

    try {
      TimeUnit.MILLISECONDS.sleep(2000);
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeMetric()));
      Awaitility.await().untilAsserted(() -> assertThat(result.isSuccess()).isTrue());
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_AfterShutdown() {
    MetricData span = generateFakeMetric();
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    exporter.shutdown();
    assertThat(exporter.export(Collections.singletonList(span)).isSuccess()).isFalse();
  }

  @Test
  void testExport_Cancelled() {
    fakeCollector.setReturnedStatus(Status.CANCELLED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_DeadlineExceeded() {
    fakeCollector.setReturnedStatus(Status.DEADLINE_EXCEEDED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_ResourceExhausted() {
    fakeCollector.setReturnedStatus(Status.RESOURCE_EXHAUSTED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_OutOfRange() {
    fakeCollector.setReturnedStatus(Status.OUT_OF_RANGE);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_Unavailable() {
    fakeCollector.setReturnedStatus(Status.UNAVAILABLE);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
    LoggingEvent log =
        logs.assertContains(
            "Failed to export metrics. Server is UNAVAILABLE. "
                + "Make sure your collector is running and reachable from this network.");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void testExport_Unimplemented() {
    fakeCollector.setReturnedStatus(Status.UNIMPLEMENTED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
    LoggingEvent log =
        logs.assertContains(
            "Failed to export metrics. Server responded with UNIMPLEMENTED. "
                + "This usually means that your collector is not configured with an otlp "
                + "receiver in the \"pipelines\" section of the configuration. "
                + "Full error message: UNIMPLEMENTED");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void testExport_DataLoss() {
    fakeCollector.setReturnedStatus(Status.DATA_LOSS);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_PermissionDenied() {
    fakeCollector.setReturnedStatus(Status.PERMISSION_DENIED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())).isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void testExport_flush() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.flush().isSuccess()).isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void usingGrpc() {
    assertThat(OtlpGrpcMetricExporter.builder().delegate)
        .isInstanceOf(DefaultGrpcExporterBuilder.class);
  }

  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.createLongSum(
        Resource.empty(),
        InstrumentationLibraryInfo.empty(),
        "name",
        "description",
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                LongPointData.create(startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }

  private static final class FakeCollector extends MetricsServiceGrpc.MetricsServiceImplBase {
    private final List<ResourceMetrics> receivedMetrics = new ArrayList<>();
    private Status returnedStatus = Status.OK;

    @Override
    public void export(
        ExportMetricsServiceRequest request,
        StreamObserver<ExportMetricsServiceResponse> responseObserver) {

      receivedMetrics.addAll(request.getResourceMetricsList());
      responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().build());
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

    List<ResourceMetrics> getReceivedMetrics() {
      return receivedMetrics;
    }

    void setReturnedStatus(Status returnedStatus) {
      this.returnedStatus = returnedStatus;
    }
  }
}
