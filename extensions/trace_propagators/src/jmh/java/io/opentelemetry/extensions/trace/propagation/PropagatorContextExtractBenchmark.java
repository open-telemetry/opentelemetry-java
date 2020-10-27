/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Arrays;
import java.util.Collections;
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
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

public class PropagatorContextExtractBenchmark {

  private PropagatorContextExtractBenchmark() {}

  /**
   * Abstract class containing common setup and teardown logic along with a benchmark to measure
   * extracting propagated trace context. Implementing subclasses will provide the sample headers
   * and the actual call to the propagator.
   */
  @State(Scope.Thread)
  public abstract static class AbstractContextExtractBenchmark {

    private final Map<String, String> carrier = new HashMap<>();
    private Integer iteration = 0;

    @Setup
    public void setup() {
      carrier.putAll(getHeaders().get(0));
    }

    @Benchmark
    @Measurement(iterations = 15, time = 1)
    @Warmup(iterations = 5, time = 1)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Fork(1)
    public Span measureExtract() {
      return Span.fromContext(doExtract());
    }

    protected abstract Context doExtract();

    protected abstract List<Map<String, String>> getHeaders();

    Map<String, String> getCarrier() {
      return carrier;
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
      this.carrier.putAll(getHeaders().get(++iteration % getHeaders().size()));
    }
  }

  /** Benchmark for extracting context from Jaeger headers. */
  public static class JaegerContextExtractBenchmark extends AbstractContextExtractBenchmark {

    private static final List<Map<String, String>> traceHeaders =
        Arrays.asList(
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "905734c59b913b4a905734c59b913b4a:9909983295041501:0:1"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "21196a77f299580e21196a77f299580e:993a97ee3691eb26:0:0"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "2e7d0ad2390617702e7d0ad239061770:d49582a2de984b86:0:1"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "905734c59b913b4a905734c59b913b4a:776ff807b787538a:0:0"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "68ec932c33b3f2ee68ec932c33b3f2ee:68ec932c33b3f2ee:0:0"));

    private final TextMapPropagator.Getter<Map<String, String>> getter =
        new TextMapPropagator.Getter<Map<String, String>>() {
          @Override
          public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
          }
        };

    private final JaegerPropagator jaegerPropagator = JaegerPropagator.getInstance();

    @Override
    protected Context doExtract() {
      return jaegerPropagator.extract(Context.current(), getCarrier(), getter);
    }

    @Override
    protected List<Map<String, String>> getHeaders() {
      return traceHeaders;
    }
  }

  /** Benchmark for extracting context from Jaeger headers which are url encoded. */
  public static class JaegerUrlEncodedContextExtractBenchmark
      extends AbstractContextExtractBenchmark {

    private static final List<Map<String, String>> traceHeaders =
        Arrays.asList(
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "905734c59b913b4a905734c59b913b4a%3A9909983295041501%3A0%3A1"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "21196a77f299580e21196a77f299580e%3A993a97ee3691eb26%3A0%3A0"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "2e7d0ad2390617702e7d0ad239061770%3Ad49582a2de984b86%3A0%3A1"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "905734c59b913b4a905734c59b913b4a%3A776ff807b787538a%3A0%3A0"),
            Collections.singletonMap(
                JaegerPropagator.PROPAGATION_HEADER,
                "68ec932c33b3f2ee68ec932c33b3f2ee%3A68ec932c33b3f2ee%3A0%3A0"));

    private final TextMapPropagator.Getter<Map<String, String>> getter =
        new TextMapPropagator.Getter<Map<String, String>>() {
          @Override
          public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
          }
        };

    private final JaegerPropagator jaegerPropagator = JaegerPropagator.getInstance();

    @Override
    protected Context doExtract() {
      return jaegerPropagator.extract(Context.current(), getCarrier(), getter);
    }

    @Override
    protected List<Map<String, String>> getHeaders() {
      return traceHeaders;
    }
  }

  /** Benchmark for extracting context from a single B3 header. */
  public static class B3SingleHeaderContextExtractBenchmark
      extends AbstractContextExtractBenchmark {

    private static final List<Map<String, String>> traceHeaders =
        Arrays.asList(
            Collections.singletonMap(
                B3Propagator.COMBINED_HEADER,
                "905734c59b913b4a905734c59b913b4a-9909983295041501-1"),
            Collections.singletonMap(
                B3Propagator.COMBINED_HEADER,
                "21196a77f299580e21196a77f299580e-993a97ee3691eb26-0"),
            Collections.singletonMap(
                B3Propagator.COMBINED_HEADER,
                "2e7d0ad2390617702e7d0ad239061770-d49582a2de984b86-1"),
            Collections.singletonMap(
                B3Propagator.COMBINED_HEADER,
                "905734c59b913b4a905734c59b913b4a-776ff807b787538a-0"),
            Collections.singletonMap(
                B3Propagator.COMBINED_HEADER,
                "68ec932c33b3f2ee68ec932c33b3f2ee-68ec932c33b3f2ee-0"));

    private final TextMapPropagator.Getter<Map<String, String>> getter =
        new TextMapPropagator.Getter<Map<String, String>>() {
          @Override
          public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
          }
        };

    private final B3Propagator b3Propagator = B3Propagator.getInstance();

    @Override
    protected Context doExtract() {
      return b3Propagator.extract(Context.current(), getCarrier(), getter);
    }

    @Override
    protected List<Map<String, String>> getHeaders() {
      return traceHeaders;
    }
  }

  /** Benchmark for extracting context from multiple B3 headers. */
  public static class B3MultipleHeaderContextExtractBenchmark
      extends AbstractContextExtractBenchmark {

    private static final List<Map<String, String>> traceHeaders;

    static {
      traceHeaders =
          Arrays.asList(
              createHeaders("905734c59b913b4a905734c59b913b4a", "9909983295041501", "1"),
              createHeaders("21196a77f299580e21196a77f299580e", "993a97ee3691eb26", "0"),
              createHeaders("2e7d0ad2390617702e7d0ad239061770", "d49582a2de984b86", "1"),
              createHeaders("905734c59b913b4a905734c59b913b4a", "776ff807b787538a", "0"),
              createHeaders("68ec932c33b3f2ee68ec932c33b3f2ee", "68ec932c33b3f2ee", "0"));
    }

    private static Map<String, String> createHeaders(
        String traceId, String spanId, String sampled) {
      Map<String, String> headers = new HashMap<>();
      headers.put(B3Propagator.TRACE_ID_HEADER, traceId);
      headers.put(B3Propagator.SPAN_ID_HEADER, spanId);
      headers.put(B3Propagator.SAMPLED_HEADER, sampled);
      return headers;
    }

    private final TextMapPropagator.Getter<Map<String, String>> getter =
        new TextMapPropagator.Getter<Map<String, String>>() {
          @Override
          public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
          }
        };

    private final B3Propagator b3Propagator = B3Propagator.getInstance();

    @Override
    protected Context doExtract() {
      return b3Propagator.extract(Context.current(), getCarrier(), getter);
    }

    @Override
    protected List<Map<String, String>> getHeaders() {
      return traceHeaders;
    }
  }
}
