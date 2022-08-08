/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.grpc.GrpcService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 20, time = 1)
@Measurement(iterations = 20, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class GrpcExporterBenchmark {
  private static final Server server =
      Server.builder()
          .service(
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
                  .build())
          .http(0)
          .build();

  private static ManagedChannel defaultGrpcChannel;

  private static GrpcExporter<TraceRequestMarshaler> defaultGrpcExporter;
  private static GrpcExporter<TraceRequestMarshaler> okhttpGrpcExporter;

  @Setup(Level.Trial)
  public void setUp() {
    server.start().join();

    defaultGrpcChannel =
        ManagedChannelBuilder.forAddress("localhost", server.activeLocalPort()).build();
    defaultGrpcExporter =
        GrpcExporter.builder(
                "otlp",
                "span",
                10,
                URI.create("http://localhost:" + server.activeLocalPort()),
                () -> MarshalerTraceServiceGrpc::newFutureStub,
                OtlpGrpcSpanExporterBuilder.GRPC_ENDPOINT_PATH)
            .setChannel(defaultGrpcChannel)
            .build();

    okhttpGrpcExporter =
        GrpcExporter.builder(
                "otlp",
                "span",
                10,
                URI.create("http://localhost:" + server.activeLocalPort()),
                () -> MarshalerTraceServiceGrpc::newFutureStub,
                OtlpGrpcSpanExporterBuilder.GRPC_ENDPOINT_PATH)
            .build();
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    defaultGrpcExporter.shutdown().join(10, TimeUnit.SECONDS);
    okhttpGrpcExporter.shutdown().join(10, TimeUnit.SECONDS);
    defaultGrpcChannel.shutdownNow();
    server.stop().join();
  }

  @Benchmark
  public CompletableResultCode defaultGrpcExporter(RequestMarshalState state) {
    CompletableResultCode result =
        defaultGrpcExporter
            .export(state.traceRequestMarshaler, state.numSpans)
            .join(10, TimeUnit.SECONDS);
    if (!result.isSuccess()) {
      throw new AssertionError();
    }
    return result;
  }

  @Benchmark
  public CompletableResultCode okhttpGrpcExporter(RequestMarshalState state) {
    CompletableResultCode result =
        okhttpGrpcExporter
            .export(state.traceRequestMarshaler, state.numSpans)
            .join(10, TimeUnit.SECONDS);
    if (!result.isSuccess()) {
      throw new AssertionError();
    }
    return result;
  }
}
