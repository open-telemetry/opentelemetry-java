/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.grpc.GrpcService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSender;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.net.URI;
import java.util.Collections;
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
@SuppressWarnings("NonFinalStaticField")
public class OltpExporterBenchmark {
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
          .service("/v1/traces", (ctx, req) -> HttpResponse.of(200))
          .http(0)
          .build();

  private static ManagedChannel defaultGrpcChannel;

  private static GrpcExporter upstreamGrpcExporter;
  private static GrpcExporter okhttpGrpcSender;
  private static HttpExporter httpExporter;

  @Setup(Level.Trial)
  public void setUp() {
    server.start().join();

    defaultGrpcChannel =
        ManagedChannelBuilder.forAddress("localhost", server.activeLocalPort())
            .usePlaintext()
            .build();
    upstreamGrpcExporter =
        new GrpcExporter(
            new UpstreamGrpcSender(
                defaultGrpcChannel,
                "opentelemetry.proto.collector.trace.v1.TraceService/Export",
                null,
                /* shutdownChannel= */ false,
                10,
                Collections::emptyMap,
                null),
            InternalTelemetryVersion.LATEST,
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER),
            MeterProvider::noop,
            URI.create("http://localhost"));

    okhttpGrpcSender =
        new GrpcExporter(
            new OkHttpGrpcSender(
                URI.create("http://localhost:" + server.activeLocalPort())
                    .resolve("opentelemetry.proto.collector.trace.v1.TraceService/Export")
                    .toString(),
                null,
                10,
                10,
                Collections::emptyMap,
                null,
                null,
                null,
                null),
            InternalTelemetryVersion.LATEST,
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER),
            MeterProvider::noop,
            URI.create("http://localhost"));

    httpExporter =
        new HttpExporterBuilder(
                StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER,
                "http://localhost:" + server.activeLocalPort() + "/v1/traces")
            .build();
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    upstreamGrpcExporter.shutdown().join(10, TimeUnit.SECONDS);
    okhttpGrpcSender.shutdown().join(10, TimeUnit.SECONDS);
    httpExporter.shutdown().join(10, TimeUnit.SECONDS);
    defaultGrpcChannel.shutdownNow();
    server.stop().join();
  }

  @Benchmark
  public CompletableResultCode defaultGrpcExporter(RequestMarshalState state) {
    CompletableResultCode result =
        upstreamGrpcExporter
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
        okhttpGrpcSender
            .export(state.traceRequestMarshaler, state.numSpans)
            .join(10, TimeUnit.SECONDS);
    if (!result.isSuccess()) {
      throw new AssertionError();
    }
    return result;
  }

  @Benchmark
  public CompletableResultCode httpExporter(RequestMarshalState state) {
    CompletableResultCode result =
        httpExporter.export(state.traceRequestMarshaler, state.numSpans).join(10, TimeUnit.SECONDS);
    if (!result.isSuccess()) {
      throw new AssertionError();
    }
    return result;
  }
}
