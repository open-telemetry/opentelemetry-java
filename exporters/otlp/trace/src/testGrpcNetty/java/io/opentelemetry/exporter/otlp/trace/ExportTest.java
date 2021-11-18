/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
  public static ServerExtension server =
      new ServerExtension() {
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
                          responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
                  .build());
          sb.http(0);
          sb.https(0);
          sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
        }
      };

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
  void builderDelegate() {
    assertThatCode(
            () ->
                DefaultGrpcExporterBuilder.getDelegateBuilder(
                        OtlpGrpcSpanExporterBuilder.class, OtlpGrpcSpanExporter.builder())
                    .addRetryPolicy(RetryPolicy.getDefault()))
        .doesNotThrowAnyException();
  }

  @Test
  void usingGrpc() {
    assertThat(OtlpGrpcSpanExporter.builder().delegate)
        .isInstanceOf(DefaultGrpcExporterBuilder.class);
  }
}
