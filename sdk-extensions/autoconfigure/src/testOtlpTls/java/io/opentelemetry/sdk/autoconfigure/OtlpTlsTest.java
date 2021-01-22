/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpTlsTest {

  private static final BlockingQueue<ExportTraceServiceRequest> otlpTraceRequests =
      new LinkedBlockingDeque<>();

  @RegisterExtension
  @Order(1)
  public static final SelfSignedCertificateExtension certificate =
      new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              GrpcService.builder()
                  // OTLP spans
                  .addService(
                      new TraceServiceGrpc.TraceServiceImplBase() {
                        @Override
                        public void export(
                            ExportTraceServiceRequest request,
                            StreamObserver<ExportTraceServiceResponse> responseObserver) {
                          otlpTraceRequests.add(request);
                          responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
                  .useBlockingTaskExecutor(true)
                  .build());

          sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
        }
      };

  @BeforeEach
  void setUp() {
    otlpTraceRequests.clear();
  }

  @Test
  void configures() {
    String endpoint = "https://localhost:" + server.httpsPort();
    System.setProperty("otel.exporter.otlp.endpoint", endpoint);
    System.setProperty("otel.exporter.otlp.timeout", "10000");
    System.setProperty(
        "otel.exporter.otlp.certificate", certificate.certificateFile().getAbsolutePath());

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await().untilAsserted(() -> assertThat(otlpTraceRequests).hasSize(1));
  }

  @Test
  void invalidCertificatePath() {
    String endpoint = "https://localhost:" + server.httpsPort();
    System.setProperty("otel.exporter.otlp.endpoint", endpoint);
    System.setProperty("otel.exporter.otlp.timeout", "10000");
    System.setProperty("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());

    assertThatThrownBy(OpenTelemetrySdkAutoConfiguration::initialize)
        .isInstanceOf(ConfigurationException.class);
  }
}
