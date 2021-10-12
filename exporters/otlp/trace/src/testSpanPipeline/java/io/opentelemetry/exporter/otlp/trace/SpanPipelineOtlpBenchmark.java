/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class SpanPipelineOtlpBenchmark {
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.builder()
              .put(AttributeKey.booleanKey("key_bool"), true)
              .put(AttributeKey.stringKey("key_string"), "string")
              .put(AttributeKey.longKey("key_int"), 100L)
              .put(AttributeKey.doubleKey("key_double"), 100.3)
              .put(
                  AttributeKey.stringArrayKey("key_string_array"),
                  Arrays.asList("string", "string"))
              .put(AttributeKey.longArrayKey("key_long_array"), Arrays.asList(12L, 23L))
              .put(AttributeKey.doubleArrayKey("key_double_array"), Arrays.asList(12.3, 23.1))
              .put(AttributeKey.booleanArrayKey("key_boolean_array"), Arrays.asList(true, false))
              .build());

  private static final Attributes SPAN_ATTRIBUTES =
      Attributes.builder()
          .put(AttributeKey.booleanKey("key_bool"), true)
          .put(AttributeKey.stringKey("key_string"), "string")
          .put(AttributeKey.longKey("key_int"), 100L)
          .put(AttributeKey.doubleKey("key_double"), 100.3)
          .build();

  private static final NoopCollector collector = new NoopCollector();
  private static final String serverName = InProcessServerBuilder.generateName();
  private static final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();
  private static final Server server;

  static {
    try {
      server =
          InProcessServerBuilder.forName(serverName)
              .directExecutor()
              .addService(collector)
              .build()
              .start();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final SdkTracerProvider tracerProvider =
      SdkTracerProvider.builder()
          .setResource(RESOURCE)
          .addSpanProcessor(
              BatchSpanProcessor.builder(
                      OtlpGrpcSpanExporter.builder().setChannel(inProcessChannel).build())
                  .setScheduleDelay(Duration.ofMillis(500))
                  .build())
          .build();

  private static final Tracer tracer = tracerProvider.get("benchmark");

  private static void tearDown() {
    tracerProvider.close();
    server.shutdownNow();
    inProcessChannel.shutdownNow();
  }

  private static void createSpan() {
    Span span = tracer.spanBuilder("POST /search").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      span.setAllAttributes(SPAN_ATTRIBUTES);
    }
    span.end();
  }

  // Convenient to run in IDE with profiling
  @Test
  void runPipeline() {
    long startTimeNanos = System.nanoTime();
    long endTimeNanos = startTimeNanos + TimeUnit.SECONDS.toNanos(60);
    try {
      while (System.nanoTime() < endTimeNanos) {
        SpanPipelineOtlpBenchmark.createSpan();
      }
    } finally {
      SpanPipelineOtlpBenchmark.tearDown();
    }
  }

  private static class NoopCollector extends TraceServiceGrpc.TraceServiceImplBase {
    @Override
    public void export(
        ExportTraceServiceRequest request,
        StreamObserver<ExportTraceServiceResponse> responseObserver) {
      responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
      responseObserver.onCompleted();
    }
  }
}
