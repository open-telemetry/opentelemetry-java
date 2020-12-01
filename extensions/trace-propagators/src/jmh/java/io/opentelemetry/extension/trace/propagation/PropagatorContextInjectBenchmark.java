/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
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
      Context context = Context.current().with(Span.wrap(contextToTest));
      doInject(context, carrier);
      return carrier;
    }

    protected abstract void doInject(Context context, Map<String, String> carrier);

    @TearDown(Level.Iteration)
    public void tearDown() {
      this.contextToTest = spanContexts.get(++iteration % spanContexts.size());
    }

    private static SpanContext createTestSpanContext(String traceId, String spanId) {
      TraceState traceStateDefault = TraceState.builder().build();
      return SpanContext.create(traceId, spanId, TraceFlags.getSampled(), traceStateDefault);
    }
  }

  /** Benchmark for injecting trace context into Jaeger headers. */
  public static class JaegerContextInjectBenchmark extends AbstractContextInjectBenchmark {

    private final JaegerPropagator jaegerPropagator = JaegerPropagator.getInstance();
    private final TextMapPropagator.Setter<Map<String, String>> setter =
        new TextMapPropagator.Setter<Map<String, String>>() {
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

    private final B3Propagator b3Propagator = B3Propagator.getInstance();
    private final TextMapPropagator.Setter<Map<String, String>> setter =
        new TextMapPropagator.Setter<Map<String, String>>() {
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

    private final B3Propagator b3Propagator =
        B3Propagator.builder().injectMultipleHeaders().build();
    private final TextMapPropagator.Setter<Map<String, String>> setter =
        new TextMapPropagator.Setter<Map<String, String>>() {
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

  /** Benchmark for injecting trace context into AWS X-Ray headers. */
  public static class AwsXRayPropagatorInjectBenchmark extends AbstractContextInjectBenchmark {
    private final AwsXRayPropagator xrayPropagator = AwsXRayPropagator.getInstance();
    private final TextMapPropagator.Setter<Map<String, String>> setter =
        new TextMapPropagator.Setter<Map<String, String>>() {
          @Override
          public void set(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
          }
        };

    @Override
    protected void doInject(Context context, Map<String, String> carrier) {
      xrayPropagator.inject(context, carrier, setter);
    }
  }
}
