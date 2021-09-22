/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.exporter.otlp.internal.grpc.GrpcStatusUtil.otlpRetryableStatusCodes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.retry.RetryPolicy;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ExportTest {

  private static final long START_NS = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
  private static final List<MetricData> METRICS =
      Collections.singletonList(
          MetricData.createLongSum(
              Resource.empty(),
              InstrumentationLibraryInfo.empty(),
              "name",
              "description",
              "1",
              LongSumData.create(
                  /* isMonotonic= */ true,
                  AggregationTemporality.CUMULATIVE,
                  Collections.singletonList(
                      LongPointData.create(
                          START_NS,
                          START_NS + TimeUnit.MILLISECONDS.toNanos(900),
                          Attributes.of(stringKey("k"), "v"),
                          5)))));

  @RegisterExtension
  @Order(1)
  public static SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  public static GrpcServer server = new GrpcServer(certificate);

  @AfterEach
  void afterEach() {
    server.reset();
  }

  @Test
  void gzipCompressionExport() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .setCompression("gzip")
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void plainTextExport() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void authorityWithAuth() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://foo:bar@localhost:" + server.httpPort())
            .build();
    CompletableResultCode resultCode = exporter.export(METRICS);
    assertThat(resultCode.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport() throws Exception {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport_untrusted() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void testRetryPolicy() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .setTimeout(Duration.ofSeconds(10))
            .setRetryPolicy(
                RetryPolicy.exponentialBackoff(4, Duration.ofMillis(100), Duration.ofSeconds(1), 2))
            .build();

    for (Status.Code code : Status.Code.values()) {
      server.reset();

      server.returnStatuses.add(Status.fromCode(code));
      server.returnStatuses.add(Status.OK);

      CompletableResultCode resultCode = exporter.export(METRICS);
      resultCode = resultCode.join(10, TimeUnit.SECONDS);
      boolean retryable = otlpRetryableStatusCodes().contains(code);
      boolean expectedResult = retryable || code == Status.Code.OK;
      assertThat(resultCode.isSuccess())
          .as(
              "status code %s should export %s",
              code, expectedResult ? "successfully" : "unsuccessfully")
          .isEqualTo(expectedResult);
      int expectedRequests = retryable ? 2 : 1;
      assertThat(server.requests.size())
          .as("status code %s should make %s requests", code, expectedRequests)
          .isEqualTo(expectedRequests);
    }
  }

  @Test
  void tlsBadCert() {
    assertThatThrownBy(
            () ->
                OtlpGrpcMetricExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8))
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Could not set trusted certificates");
  }

  private static class GrpcServer extends ServerExtension {

    private final SelfSignedCertificateExtension certificate;
    private final Deque<ExportMetricsServiceRequest> requests = new ArrayDeque<>();
    private final Deque<Status> returnStatuses = new ArrayDeque<>();

    private GrpcServer(SelfSignedCertificateExtension certificate) {
      this.certificate = certificate;
    }

    @Override
    protected void configure(ServerBuilder sb) {
      sb.service(
          GrpcService.builder()
              .addService(
                  new MetricsServiceGrpc.MetricsServiceImplBase() {
                    @Override
                    public void export(
                        ExportMetricsServiceRequest request,
                        StreamObserver<ExportMetricsServiceResponse> responseObserver) {
                      requests.offer(request);
                      Status returnStatus =
                          returnStatuses.peek() != null ? returnStatuses.poll() : Status.OK;
                      if (returnStatus.isOk()) {
                        responseObserver.onNext(ExportMetricsServiceResponse.getDefaultInstance());
                        responseObserver.onCompleted();
                        return;
                      }
                      responseObserver.onError(returnStatus.asRuntimeException());
                    }
                  })
              .build());
      sb.http(0);
      sb.https(0);
      sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
    }

    public void reset() {
      requests.clear();
      returnStatuses.clear();
    }
  }
}
