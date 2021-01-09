/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class SpanBenchmark {
  private static SdkSpanBuilder sdkSpanBuilder;
  private final Resource serviceResource =
      Resource.create(
          Attributes.builder()
              .put("service.name", "benchmark1")
              .put("service.version", "123.456.89")
              .put("service.instance.id", "123ab456-a123-12ab-12ab-12340a1abc12")
              .build());

  @Setup(Level.Trial)
  public final void setup() {

    TraceConfig alwaysOn = TraceConfig.builder().setSampler(Sampler.alwaysOn()).build();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder().setResource(serviceResource).setTraceConfig(alwaysOn).build();

    Tracer tracerSdk = tracerProvider.get("benchmarkTracer");
    sdkSpanBuilder =
        (SdkSpanBuilder)
            tracerSdk.spanBuilder("benchmarkSpanBuilder").setAttribute("longAttribute", 33L);
  }

  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void simpleSpanStartAddEventEnd_01Thread() {
    doSpanWork();
  }

  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void simpleSpanStartAddEventEnd_05Threads() {
    doSpanWork();
  }

  @Benchmark
  @Threads(value = 2)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void simpleSpanStartAddEventEnd_02Threads() {
    doSpanWork();
  }

  @Benchmark
  @Threads(value = 10)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void simpleSpanStartAddEventEnd_10Threads() {
    doSpanWork();
  }

  private static void doSpanWork() {
    Span span = sdkSpanBuilder.startSpan();
    span.addEvent("testEvent");
    span.end();
  }
}
