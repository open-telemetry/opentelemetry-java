/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporter.jaeger.proto.api_v2.CollectorServiceGrpc;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class JaegerConfigTest {

  private static final BlockingQueue<Collector.PostSpansRequest> jaegerRequests =
      new LinkedBlockingDeque<>();

  @RegisterExtension
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              GrpcService.builder()
                  // Jaeger
                  .addService(
                      new CollectorServiceGrpc.CollectorServiceImplBase() {
                        @Override
                        public void postSpans(
                            Collector.PostSpansRequest request,
                            StreamObserver<Collector.PostSpansResponse> responseObserver) {
                          jaegerRequests.add(request);
                          responseObserver.onNext(Collector.PostSpansResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
                  .useBlockingTaskExecutor(true)
                  .build());
        }
      };

  @BeforeEach
  void setUp() {
    jaegerRequests.clear();
  }

  @Test
  void configures() {
    String endpoint = "http://localhost:" + server.httpPort();

    System.setProperty("otel.exporter.jaeger.endpoint", endpoint);

    AutoConfiguredSdk.initialize();

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await().untilAsserted(() -> assertThat(jaegerRequests).hasSize(1));
  }
}
