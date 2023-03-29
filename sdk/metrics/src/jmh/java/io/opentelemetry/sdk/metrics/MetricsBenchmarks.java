/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.ThreadParams;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class MetricsBenchmarks {

  private static final List<Attributes> ATTRIBUTES_LIST;

  static {
    int keys = 5;
    int valuesPerKey = 20;

    ATTRIBUTES_LIST = new ArrayList<>();
    for (int key = 0; key < keys; key++) {
      AttributesBuilder builder = Attributes.builder();
      for (int value = 0; value < valuesPerKey; value++) {
        builder.put("key_" + key, "value_" + value);
      }
      ATTRIBUTES_LIST.add(builder.build());
    }
  }

  @State(Scope.Thread)
  public static class ThreadState {

    @Param TestSdk sdk;

    @Param MetricsTestOperationBuilder opBuilder;

    MetricsTestOperationBuilder.Operation op;
    Span span;
    io.opentelemetry.context.Scope contextScope;
    final Attributes sharedLabelSet = Attributes.builder().put("KEY", "VALUE").build();
    Attributes threadUniqueLabelSet;

    @Setup
    @SuppressWarnings("MustBeClosedChecker")
    public void setup(ThreadParams threadParams) {
      Meter meter = sdk.getMeter();
      Tracer tracer = sdk.getTracer();
      span = tracer.spanBuilder("benchmark").startSpan();
      // We suppress warnings on closing here, as we rely on tests to make sure context is closed.
      contextScope = span.makeCurrent();
      op = opBuilder.build(meter);
      threadUniqueLabelSet =
          Attributes.builder().put("KEY", String.valueOf(threadParams.getThreadIndex())).build();
    }

    @TearDown
    public void tearDown(ThreadParams threadParams) {
      contextScope.close();
      span.end();
    }
  }

  @Benchmark
  @Threads(1)
  public void recordToMultipleAttributes(ThreadState threadState) {
    for (Attributes attributes : ATTRIBUTES_LIST) {
      threadState.op.perform(attributes);
    }
  }

  @Benchmark
  @Threads(1)
  public void oneThread(ThreadState threadState) {
    threadState.op.perform(threadState.sharedLabelSet);
  }

  @Benchmark
  @Threads(8)
  public void eightThreadsCommonLabelSet(ThreadState threadState) {
    threadState.op.perform(threadState.sharedLabelSet);
  }

  @Benchmark
  @Threads(8)
  public void eightThreadsSeparateLabelSets(ThreadState threadState) {
    threadState.op.perform(threadState.threadUniqueLabelSet);
  }
}
