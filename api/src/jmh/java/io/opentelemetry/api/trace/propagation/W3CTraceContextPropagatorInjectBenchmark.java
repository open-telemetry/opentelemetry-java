/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator.Setter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class W3CTraceContextPropagatorInjectBenchmark {

  private static final List<SpanContext> spanContexts =
      Arrays.asList(
          createTestSpanContext("905734c59b913b4a905734c59b913b4a", "9909983295041501"),
          createTestSpanContext("21196a77f299580e21196a77f299580e", "993a97ee3691eb26"),
          createTestSpanContext("2e7d0ad2390617702e7d0ad239061770", "d49582a2de984b86"),
          createTestSpanContext("905734c59b913b4a905734c59b913b4a", "776ff807b787538a"),
          createTestSpanContext("68ec932c33b3f2ee68ec932c33b3f2ee", "68ec932c33b3f2ee"));
  private static final int COUNT = 5; // spanContexts.size()
  private final TextMapPropagator w3cTraceContextPropagator =
      W3CTraceContextPropagator.getInstance();
  private final Map<String, String> carrier = new HashMap<>();
  private final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void set(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };
  private final List<Context> contexts = createContexts(spanContexts);

  /** Benchmark for measuring inject with default trace state and sampled trace options. */
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  @OperationsPerInvocation(COUNT)
  public Map<String, String> measureInject() {
    for (int i = 0; i < COUNT; i++) {
      w3cTraceContextPropagator.inject(contexts.get(i), carrier, setter);
    }
    return carrier;
  }

  private static SpanContext createTestSpanContext(String traceId, String spanId) {
    byte sampledTraceOptions = TraceFlags.getSampled();
    TraceState traceStateDefault = TraceState.builder().build();
    return SpanContext.create(traceId, spanId, sampledTraceOptions, traceStateDefault);
  }

  private static List<Context> createContexts(List<SpanContext> spanContexts) {
    List<Context> contexts = new ArrayList<>();
    for (SpanContext context : spanContexts) {
      contexts.add(Context.root().with(Span.wrap(context)));
    }
    return contexts;
  }
}
