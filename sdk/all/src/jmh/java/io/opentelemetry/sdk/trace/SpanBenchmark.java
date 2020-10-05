/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.StatusCanonicalCode;
import io.opentelemetry.trace.Tracer;
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

  private final Tracer tracerSdk = OpenTelemetry.getTracer("benchmarkTracer");
  private RecordEventsReadableSpan span;

  @Setup(Level.Trial)
  public final void setup() {
    SpanBuilderSdk spanBuilderSdk =
        (SpanBuilderSdk)
            tracerSdk
                .spanBuilder("benchmarkSpan")
                .setSpanKind(Kind.CLIENT)
                .setAttribute("key", "value");
    span = (RecordEventsReadableSpan) spanBuilderSdk.startSpan();
  }

  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addAttributesEventsStatusEnd_01Thread() {
    doSpanWork(span);
  }

  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addAttributesEventsStatusEnd_05Threads() {
    doSpanWork(span);
  }

  @Benchmark
  @Threads(value = 2)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addAttributesEventsStatusEnd_02Threads() {
    doSpanWork(span);
  }

  @Benchmark
  @Threads(value = 10)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addAttributesEventsStatusEnd_10Threads() {
    doSpanWork(span);
  }

  private static void doSpanWork(RecordEventsReadableSpan span) {
    span.setAttribute("longAttribute", 33L);
    span.setAttribute("stringAttribute", "test_value");
    span.setStatus(StatusCanonicalCode.OK);

    span.addEvent("testEvent");
    span.end();
  }
}
