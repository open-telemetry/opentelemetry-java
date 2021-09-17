/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.opentelemetry.exporter.otlp.internal.RetryUtil.retryableStatusCodes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.RetryConfig;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
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

  private static final List<SpanData> SPANS =
      Collections.singletonList(
          TestSpanData.builder()
              .setName("name")
              .setKind(SpanKind.CLIENT)
              .setStartEpochNanos(1)
              .setEndEpochNanos(2)
              .setStatus(StatusData.ok())
              .setHasEnded(true)
              .build());

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
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .setCompression("gzip")
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void plainTextExport() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost:" + server.httpPort()).build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void authorityWithAuth() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://foo:bar@localhost:" + server.httpPort())
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport() throws Exception {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport_untrusted() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void tlsBadCert() {
    assertThatThrownBy(
            () ->
                OtlpGrpcSpanExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8))
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Could not set trusted certificates");
  }

  @Test
  void testRetryPolicy() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .setTimeout(Duration.ofSeconds(5))
            .setRetryConfig(
                RetryConfig.exponentialBackoff(
                    5, Duration.ofMillis(500), Duration.ofSeconds(30), 2, Duration.ofMillis(20)))
            .build();

    for (Status.Code code : Status.Code.values()) {
      server.reset();

      server.returnStatuses.add(Status.fromCode(code));
      server.returnStatuses.add(Status.OK);

      CompletableResultCode resultCode = exporter.export(SPANS).join(10, TimeUnit.SECONDS);
      boolean retryable = retryableStatusCodes().contains(code);
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

  private static class GrpcServer extends ServerExtension {

    private final SelfSignedCertificateExtension certificate;
    private final Deque<ExportTraceServiceRequest> requests = new ArrayDeque<>();
    private final Deque<Status> returnStatuses = new ArrayDeque<>();

    private GrpcServer(SelfSignedCertificateExtension certificate) {
      this.certificate = certificate;
    }

    @Override
    protected void configure(ServerBuilder sb) {
      sb.service(
          GrpcService.builder()
              .addService(
                  new TraceServiceGrpc.TraceServiceImplBase() {
                    @Override
                    public void export(
                        ExportTraceServiceRequest request,
                        StreamObserver<ExportTraceServiceResponse> responseObserver) {
                      requests.offer(request);
                      Status returnStatus =
                          returnStatuses.peek() != null ? returnStatuses.poll() : Status.OK;
                      if (returnStatus.isOk()) {
                        responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
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
