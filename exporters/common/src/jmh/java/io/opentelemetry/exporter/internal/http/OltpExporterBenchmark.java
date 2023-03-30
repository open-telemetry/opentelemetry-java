/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import com.linecorp.armeria.common.AggregationOptions;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.Server;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.exporter.otlp.trace.MarshalerTraceServiceGrpc;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public class OltpExporterBenchmark {
  private static final MediaType GRPC_PROTO = MediaType.parse("application/grpc+proto");
  private static final List<Map.Entry<String, String>> GRPC_TRAILERS =
      Collections.singletonList(new AbstractMap.SimpleEntry<>("grpc-status", "0"));
  private static final byte[] GRPC_CONTENT = new byte[] {0, 0, 0, 0, 0};

  private static final Server server =
      Server.builder()
          .service(
              OtlpGrpcSpanExporterBuilder.GRPC_ENDPOINT_PATH,
              (ctx, req) ->
                  HttpResponse.from(
                      req.aggregate(AggregationOptions.builder().usePooledObjects().build())
                          .thenApply(
                              aggregatedHttpRequest -> {
                                try (HttpData unused = aggregatedHttpRequest.content()) {
                                  return HttpResponse.builder()
                                      .status(200)
                                      .trailers(GRPC_TRAILERS)
                                      .content(GRPC_PROTO, GRPC_CONTENT)
                                      .build();
                                }
                              })))
          .service(
              "/v1/traces",
              (ctx, req) ->
                  HttpResponse.from(
                      req.aggregate(AggregationOptions.builder().usePooledObjects().build())
                          .thenApply(
                              aggregatedHttpRequest -> {
                                try (HttpData unused = aggregatedHttpRequest.content()) {
                                  return HttpResponse.builder().status(200).build();
                                }
                              })))
          .http(0)
          .build();

  private static ManagedChannel defaultGrpcChannel;

  private static GrpcExporter<TraceRequestMarshaler> defaultGrpcExporter;
  private static GrpcExporter<TraceRequestMarshaler> okhttpGrpcExporter;
  private static HttpExporter<TraceRequestMarshaler> okHttpHttpExporter;
  private static HttpExporter<TraceRequestMarshaler> jdkHttpExporter;

  @Setup(Level.Trial)
  public void setUp() {
    server.start().join();

    defaultGrpcChannel =
        ManagedChannelBuilder.forAddress("localhost", server.activeLocalPort())
            .usePlaintext()
            .build();
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

    okHttpHttpExporter =
        new HttpExporter<>(
            "otlp",
            "span",
            new OkHttpSender(
                "http://localhost:" + server.activeLocalPort() + "/v1/traces",
                false,
                TimeUnit.SECONDS.toNanos(10),
                Collections::emptyMap,
                null,
                null,
                null),
            MeterProvider::noop,
            false);

    HttpSender jdkHttpSender =
        HttpSender.create(
            "http://localhost:" + server.activeLocalPort() + "/v1/traces",
            false,
            TimeUnit.SECONDS.toNanos(10),
            Collections::emptyMap,
            null,
            null,
            null,
            null);
    if (!jdkHttpSender.getClass().getSimpleName().equals("JdkHttpSender")) {
      throw new IllegalStateException("Must run with java 11+: -PtestJavaVersion=11");
    }
    jdkHttpExporter = new HttpExporter<>("otlp", "span", jdkHttpSender, MeterProvider::noop, false);
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    defaultGrpcExporter.shutdown().join(10, TimeUnit.SECONDS);
    okhttpGrpcExporter.shutdown().join(10, TimeUnit.SECONDS);
    okHttpHttpExporter.shutdown().join(10, TimeUnit.SECONDS);
    jdkHttpExporter.shutdown().join(10, TimeUnit.SECONDS);
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

  @Benchmark
  public CompletableResultCode okHttpHttpExporter(RequestMarshalState state) {
    CompletableResultCode result =
        okHttpHttpExporter
            .export(state.traceRequestMarshaler, state.numSpans)
            .join(10, TimeUnit.SECONDS);
    if (!result.isSuccess()) {
      throw new AssertionError();
    }
    return result;
  }

  @Benchmark
  public CompletableResultCode jdkHttpExporter(RequestMarshalState state) {
    CompletableResultCode result =
        jdkHttpExporter
            .export(state.traceRequestMarshaler, state.numSpans)
            .join(10, TimeUnit.SECONDS);
    if (!result.isSuccess()) {
      throw new AssertionError();
    }
    return result;
  }
}
