/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ExportTest {

  private static final List<LogData> LOGS =
      Collections.singletonList(
          LogData.builder(
                  Resource.create(Attributes.builder().put("testKey", "testValue").build()),
                  InstrumentationLibraryInfo.create("instrumentation", "1"))
              .setEpoch(Instant.now())
              .setSeverity(Severity.ERROR)
              .setSeverityText("really severe")
              .setName("log1")
              .setBody("message")
              .setAttributes(Attributes.builder().put("animal", "cat").build())
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
                      new LogsServiceGrpc.LogsServiceImplBase() {
                        @Override
                        public void export(
                            ExportLogsServiceRequest request,
                            StreamObserver<ExportLogsServiceResponse> responseObserver) {
                          responseObserver.onNext(ExportLogsServiceResponse.getDefaultInstance());
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
    OtlpGrpcLogExporter exporter =
        OtlpGrpcLogExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .setCompression("gzip")
            .build();
    assertThat(exporter.export(LOGS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void plainTextExport() {
    OtlpGrpcLogExporter exporter =
        OtlpGrpcLogExporter.builder().setEndpoint("http://localhost:" + server.httpPort()).build();
    assertThat(exporter.export(LOGS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void authorityWithAuth() {
    OtlpGrpcLogExporter exporter =
        OtlpGrpcLogExporter.builder()
            .setEndpoint("http://foo:bar@localhost:" + server.httpPort())
            .build();
    assertThat(exporter.export(LOGS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport() throws Exception {
    OtlpGrpcLogExporter exporter =
        OtlpGrpcLogExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .build();
    assertThat(exporter.export(LOGS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport_untrusted() {
    OtlpGrpcLogExporter exporter =
        OtlpGrpcLogExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .build();
    assertThat(exporter.export(LOGS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void tlsBadCert() {
    assertThatThrownBy(
            () ->
                OtlpGrpcLogExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8))
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Could not set trusted certificates");
  }

  @Test
  void usingGrpc() {
    assertThat(OtlpGrpcLogExporter.builder().delegate)
        .isInstanceOf(DefaultGrpcExporterBuilder.class);
  }
}
