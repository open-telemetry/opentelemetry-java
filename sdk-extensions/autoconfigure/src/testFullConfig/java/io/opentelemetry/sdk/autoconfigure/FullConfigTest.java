/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporter.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FullConfigTest {

  private static final BlockingQueue<Collector.PostSpansRequest> jaegerRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<ExportTraceServiceRequest> otlpTraceRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<ExportMetricsServiceRequest> otlpMetricsRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<String> zipkinJsonRequests = new LinkedBlockingDeque<>();

  @RegisterExtension
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
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
                  // OTLP metrics
                  .addService(
                      new MetricsServiceGrpc.MetricsServiceImplBase() {
                        @Override
                        public void export(
                            ExportMetricsServiceRequest request,
                            StreamObserver<ExportMetricsServiceResponse> responseObserver) {
                          if (request.getResourceMetricsCount() > 0) {
                            otlpMetricsRequests.add(request);
                          }
                          responseObserver.onNext(
                              ExportMetricsServiceResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
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
                  .build());

          // Zipkin
          sb.service(
              "/api/v2/spans",
              (ctx, req) ->
                  HttpResponse.from(
                      req.aggregate()
                          .thenApply(
                              aggRes -> {
                                zipkinJsonRequests.add(aggRes.contentUtf8());
                                return HttpResponse.of(HttpStatus.OK);
                              })));
        }
      };

  @Test
  void configures() {
    // We can't configure endpoint declaratively as would be normal in non-test environments.
    String endpoint = "localhost:" + server.httpPort();
    System.setProperty("otel.exporter.otlp.endpoint", endpoint);
    System.setProperty("otel.exporter.otlp.insecure", "true");

    System.setProperty("otel.exporter.jaeger.endpoint", endpoint);

    System.setProperty("otel.exporter.zipkin.endpoint", "http://" + endpoint + "/api/v2/spans");

    OpenTelemetry openTelemetry = OpenTelemetrySdkAutoConfiguration.initialize();
    openTelemetry.getTracer("test").spanBuilder("test").startSpan().end();

    await()
        .untilAsserted(
            () -> {
              assertThat(jaegerRequests).hasSize(1);
              assertThat(otlpTraceRequests).hasSize(1);
              assertThat(zipkinJsonRequests).hasSize(1);

              // Not well defined how many metric exports would have happened by now, check that any
              // did.
              // The metrics will be BatchSpanProcessor metrics.
              // TODO(anuraaga): WIP
              // assertThat(otlpMetricsRequests).isNotEmpty();
            });
  }
}
