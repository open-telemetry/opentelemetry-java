/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

public class PropagatorContextInjectBenchmark {

  private PropagatorContextInjectBenchmark() {}

  /**
   * Abstract class containing common setup and teardown logic along with a benchmark to measure
   * injecting trace context. Implementing subclasses will provide the actual call to the
   * propagator.
   */
  @State(Scope.Thread)
  public abstract static class AbstractContextInjectBenchmark {

    private static final List<SpanContext> spanContexts =
        Arrays.asList(
            createTestSpanContext("905734c59b913b4a905734c59b913b4a", "9909983295041501"),
            createTestSpanContext("21196a77f299580e21196a77f299580e", "993a97ee3691eb26"),
            createTestSpanContext("2e7d0ad2390617702e7d0ad239061770", "d49582a2de984b86"),
            createTestSpanContext("905734c59b913b4a905734c59b913b4a", "776ff807b787538a"),
            createTestSpanContext("68ec932c33b3f2ee68ec932c33b3f2ee", "68ec932c33b3f2ee"));

    private final Map<String, String> carrier = new HashMap<>();

    private Integer iteration = 0;
    private SpanContext contextToTest = spanContexts.get(iteration);

    /** Benchmark for measuring inject with default trace state and sampled trace options. */
    @Benchmark
    @Measurement(iterations = 15, time = 1)
    @Warmup(iterations = 5, time = 1)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Fork(1)
    public Map<String, String> measureInject() {
      Context context =
          TracingContextUtils.withSpan(DefaultSpan.create(contextToTest), Context.current());
      doInject(context, carrier);
      return carrier;
    }

    protected abstract void doInject(Context context, Map<String, String> carrier);

    @TearDown(Level.Iteration)
    public void tearDown() {
      this.contextToTest = spanContexts.get(++iteration % spanContexts.size());
    }

    private static SpanContext createTestSpanContext(String traceId, String spanId) {
      byte sampledTraceOptionsBytes = 1;
      TraceFlags sampledTraceOptions = TraceFlags.fromByte(sampledTraceOptionsBytes);
      TraceState traceStateDefault = TraceState.builder().build();
      return SpanContext.create(
          TraceId.fromLowerBase16(traceId, 0),
          SpanId.fromLowerBase16(spanId, 0),
          sampledTraceOptions,
          traceStateDefault);
    }
  }

  /** Benchmark for injecting trace context into Jaeger headers. */
  public static class JaegerContextInjectBenchmark extends AbstractContextInjectBenchmark {

    private final JaegerPropagator jaegerPropagator = new JaegerPropagator();
    private final JaegerPropagator.Setter<Map<String, String>> setter =
        new JaegerPropagator.Setter<Map<String, String>>() {
          @Override
          public void set(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
          }
        };

    @Override
    protected void doInject(Context context, Map<String, String> carrier) {
      jaegerPropagator.inject(context, carrier, setter);
    }
  }

  /** Benchmark for injecting trace context into a single B3 header. */
  public static class B3SingleHeaderContextInjectBenchmark extends AbstractContextInjectBenchmark {

    private final B3Propagator b3Propagator = new B3Propagator(true);
    private final B3Propagator.Setter<Map<String, String>> setter =
        new B3Propagator.Setter<Map<String, String>>() {
          @Override
          public void set(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
          }
        };

    @Override
    protected void doInject(Context context, Map<String, String> carrier) {
      b3Propagator.inject(context, carrier, setter);
    }
  }

  /** Benchmark for injecting trace context into multiple B3 headers. */
  public static class B3MultipleHeaderContextInjectBenchmark
      extends AbstractContextInjectBenchmark {

    private final B3Propagator b3Propagator = new B3Propagator();
    private final B3Propagator.Setter<Map<String, String>> setter =
        new B3Propagator.Setter<Map<String, String>>() {
          @Override
          public void set(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
          }
        };

    @Override
    protected void doInject(Context context, Map<String, String> carrier) {
      b3Propagator.inject(context, carrier, setter);
    }
  }
}
