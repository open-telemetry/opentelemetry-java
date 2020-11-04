/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.ReadableSpan;
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

/** Benchmark class for {@link TracezSpanBuckets}. */
@State(Scope.Benchmark)
public class TracezSpanBucketsBenchmark {

  private static final String spanName = "BENCHMARK_SPAN";
  private static ReadableSpan readableSpan;
  private TracezSpanBuckets bucket;

  @Setup(Level.Trial)
  public final void setup() {
    bucket = new TracezSpanBuckets();
    Tracer tracer = OpenTelemetry.getGlobalTracer("TracezZPageBenchmark");
    Span span = tracer.spanBuilder(spanName).startSpan();
    span.end();
    readableSpan = (ReadableSpan) span;
  }

  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_01Thread() {
    bucket.addToBucket(readableSpan);
  }

  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_05Threads() {
    bucket.addToBucket(readableSpan);
  }

  @Benchmark
  @Threads(value = 10)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_10Threads() {
    bucket.addToBucket(readableSpan);
  }

  @Benchmark
  @Threads(value = 20)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_20Threads() {
    bucket.addToBucket(readableSpan);
  }
}
