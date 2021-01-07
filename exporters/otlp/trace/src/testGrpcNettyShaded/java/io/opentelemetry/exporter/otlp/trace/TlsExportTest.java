/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TlsExportTest {

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
          sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
        }
      };

  @Test
  void testTlsExport() throws Exception {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("localhost:" + server.httpsPort())
            .setUseTls(true)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .build();
    assertThat(
            exporter
                .export(
                    Arrays.asList(
                        TestSpanData.builder()
                            .setTraceId(TraceId.getInvalid())
                            .setSpanId(SpanId.getInvalid())
                            .setName("name")
                            .setKind(Span.Kind.CLIENT)
                            .setStartEpochNanos(1)
                            .setEndEpochNanos(2)
                            .setStatus(SpanData.Status.ok())
                            .setHasEnded(true)
                            .build()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
  }

  @Test
  void testTlsExport_untrusted() throws Exception {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("localhost:" + server.httpsPort())
            .setUseTls(true)
            .build();
    assertThat(
            exporter
                .export(
                    Arrays.asList(
                        TestSpanData.builder()
                            .setTraceId(TraceId.getInvalid())
                            .setSpanId(SpanId.getInvalid())
                            .setName("name")
                            .setKind(Span.Kind.CLIENT)
                            .setStartEpochNanos(1)
                            .setEndEpochNanos(2)
                            .setStatus(SpanData.Status.ok())
                            .setHasEnded(true)
                            .build()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
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
}
