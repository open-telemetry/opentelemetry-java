/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.incubator.propagation.PassThroughPropagator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Threads(value = 1)
@Fork(3)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 20, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PassThroughPropagatorBenchmark {

  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(
          "0102030405060708090a0b0c0d0e0f00",
          "090a0b0c0d0e0f00",
          TraceFlags.getDefault(),
          TraceState.getDefault());
  private static final Map<String, String> INCOMING;

  static {
    Map<String, String> incoming = new HashMap<>();
    W3CTraceContextPropagator.getInstance()
        .inject(Context.groot().with(Span.wrap(SPAN_CONTEXT)), incoming, Map::put);
    INCOMING = Collections.unmodifiableMap(incoming);
  }

  private static final TextMapGetter<Map<String, String>> getter =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private static final TextMapPropagator passthrough =
      PassThroughPropagator.create(W3CTraceContextPropagator.getInstance().fields());

  @Benchmark
  public void passthrough() {
    Context extracted = passthrough.extract(Context.groot(), INCOMING, getter);
    passthrough.inject(extracted, new HashMap<>(), Map::put);
  }

  @Benchmark
  public void parse() {
    Context extracted =
        W3CTraceContextPropagator.getInstance().extract(Context.groot(), INCOMING, getter);
    W3CTraceContextPropagator.getInstance().inject(extracted, new HashMap<>(), Map::put);
  }
}
