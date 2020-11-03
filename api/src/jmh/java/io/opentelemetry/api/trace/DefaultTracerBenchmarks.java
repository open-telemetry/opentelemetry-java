/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class DefaultTracerBenchmarks {

  private final Tracer tracer = Tracer.getDefault();
  @Nullable private Span span = null;

  /** Benchmark the full span lifecycle. */
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public void measureFullSpanLifecycle() {
    span = tracer.spanBuilder("span").startSpan();
    try (io.opentelemetry.context.Scope ignored = span.makeCurrent()) {
      // no-op
    } finally {
      span.end();
    }
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public void measureSpanBuilding() {
    span = tracer.spanBuilder("span").startSpan();
  }

  /** Benchmark just the scope lifecycle. */
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public void measureScopeLifecycle() {
    try (io.opentelemetry.context.Scope ignored = span.makeCurrent()) {
      // no-op
    }
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public void measureGetCurrentSpan() {
    Span.current();
  }

  @TearDown(Level.Iteration)
  public void tearDown() {
    if (span != null) {
      span.end();
    }
  }
}
