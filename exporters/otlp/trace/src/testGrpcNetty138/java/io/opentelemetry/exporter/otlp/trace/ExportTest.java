/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

// gRPC 1.38.0 use a less optimized codepath which is verified here..
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
        }
      };

  @Test
  void plainTextExport() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost:" + server.httpPort()).build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(MarshalerInputStream.WRITE_TO_BYTEBUFFER).isFalse();
  }
}
