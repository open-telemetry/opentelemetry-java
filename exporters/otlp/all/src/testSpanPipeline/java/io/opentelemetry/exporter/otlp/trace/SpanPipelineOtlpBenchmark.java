/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SpanPipelineOtlpBenchmark {
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

  @RegisterExtension
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              "/opentelemetry.proto.collector.trace.v1.TraceService/Export",
              new AbstractUnaryGrpcService() {
                @Override
                protected CompletionStage<byte[]> handleMessage(
                    ServiceRequestContext ctx, byte[] message) {
                  return CompletableFuture.completedFuture(
                      ExportTraceServiceResponse.getDefaultInstance().toByteArray());
                }
              });
        }
      };

  private static SdkTracerProvider tracerProvider;
  private static Tracer tracer;

  @BeforeAll
  public static void setUp() {
    tracerProvider =
        SdkTracerProvider.builder()
            .setResource(RESOURCE)
            .addSpanProcessor(
                BatchSpanProcessor.builder(
                        OtlpGrpcSpanExporter.builder()
                            .setEndpoint(server.httpUri().toString())
                            .build())
                    .setScheduleDelay(Duration.ofMillis(500))
                    .build())
            .build();
    tracer = tracerProvider.get("benchmark");
  }

  @AfterAll
  public static void tearDown() {
    tracerProvider.close();
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
}
