/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static com.google.common.base.Preconditions.checkArgument;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBenchmarkState.ProducerState;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.infra.ThreadParams;

/**
 * Samples the public span-end call. Span creation and deadline-based pacing are outside the timed
 * payload. Invocation-fixture timing overhead limits interpretation of sub-microsecond samples.
 */
@Fork(3)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BatchSpanProcessorSpanEndBenchmark {
  @State(Scope.Thread)
  public static class SpanState extends ProducerState {
    @Param({"10000"})
    public int targetSpansPerSecond;

    private long periodNanos;
    private long nextStart;
    Span span;

    @Setup(Level.Trial)
    public void setup(BatchSpanProcessorBenchmarkState state, ThreadParams params) {
      if (targetSpansPerSecond <= 0 || targetSpansPerSecond > 1_000_000_000) {
        throw new IllegalArgumentException("Target rate must be between 1 and 1,000,000,000");
      }
      periodNanos = TimeUnit.SECONDS.toNanos(1) / targetSpansPerSecond;
      register(state, params.getThreadIndex());
      if (params.getThreadIndex() == 0) {
        state.requireNoDrops = true;
      }
    }

    @Setup(Level.Iteration)
    public void startSchedule(IterationParams params) {
      checkArgument(
          params.getBatchSize() == 1, "Span-end latency requires a JMH batch size of one");
      nextStart = System.nanoTime();
    }

    @Setup(Level.Invocation)
    public void prepareSpan() {
      long remaining;
      while ((remaining = nextStart - System.nanoTime()) > 0) {
        LockSupport.parkNanos(remaining);
      }
      long now = System.nanoTime();
      if (now - nextStart >= periodNanos) {
        // Skip missed arrival slots instead of turning a scheduling pause into a catch-up burst.
        skippedSlots += (now - nextStart) / periodNanos;
        nextStart = now;
      }
      nextStart += periodNanos;
      span = benchmarkState.tracer.spanBuilder("span").startSpan();
      attemptedSpans++;
    }
  }

  @Benchmark
  @Threads(1)
  public void end_01Thread(SpanState producer) {
    producer.span.end();
  }

  @Benchmark
  @Threads(10)
  public void end_10Thread(SpanState producer) {
    producer.span.end();
  }
}
