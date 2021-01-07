/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static com.google.common.base.Charsets.US_ASCII;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static org.assertj.core.api.Assertions.assertThat;

import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.GrpcService;
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
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpGrpcSpanExporterTest {

  @RegisterExtension
  public static ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
          sb.service(
              GrpcService.builder()
                  .addService(
                      new TraceServiceGrpc.TraceServiceImplBase() {
                        @Override
                        public void export(
                            ExportTraceServiceRequest request,
                            StreamObserver<ExportTraceServiceResponse> responseObserver) {
                          RequestHeaders headers =
                              ServiceRequestContext.current().request().headers();
                          if (headers.get("key").equals("value")
                              && headers.get("key2").equals("value2=")
                              && headers.get("key3").equals("val=ue3")
                              && headers.get("key4").equals("value4")
                              && !headers.contains("key5")) {
                            responseObserver.onNext(
                                ExportTraceServiceResponse.getDefaultInstance());
                            responseObserver.onCompleted();
                          } else {
                            responseObserver.onError(new AssertionError("Invalid metadata"));
                          }
                        }
                      })
                  .build());
        }
      };

  @Test
  void configTest() {
    Properties options = new Properties();
    String endpoint = "localhost:" + server.httpPort();
    options.put("otel.exporter.otlp.span.timeout", "5124");
    options.put("otel.exporter.otlp.span.endpoint", endpoint);
    options.put("otel.exporter.otlp.span.insecure", "true");
    options.put(
        "otel.exporter.otlp.span.headers",
        "key=value,key2=value2=,key3=val=ue3, key4 = value4 ,key5= ");
    OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder().readProperties(options);
    assertThat(builder)
        .extracting("metadata")
        .extracting("namesAndValues")
        .isEqualTo(
            new Object[] {
              "key".getBytes(US_ASCII),
              ASCII_STRING_MARSHALLER.toAsciiString("value").getBytes(US_ASCII),
              "key2".getBytes(US_ASCII),
              ASCII_STRING_MARSHALLER.toAsciiString("value2=").getBytes(US_ASCII),
              "key3".getBytes(US_ASCII),
              ASCII_STRING_MARSHALLER.toAsciiString("val=ue3").getBytes(US_ASCII),
              "key4".getBytes(US_ASCII),
              ASCII_STRING_MARSHALLER.toAsciiString("value4").getBytes(US_ASCII)
            });
    OtlpGrpcSpanExporter exporter = builder.build();

    assertThat(exporter.getTimeoutNanos()).isEqualTo(5124);
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
}
