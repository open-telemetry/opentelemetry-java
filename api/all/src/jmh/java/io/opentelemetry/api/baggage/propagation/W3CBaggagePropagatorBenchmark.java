/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@SuppressWarnings("JavadocMethod")
@State(Scope.Thread)
public class W3CBaggagePropagatorBenchmark {

  private static final Map<String, String> SMALL_BAGGAGE;
  private static final Map<String, String> LARGE_BAGGAGE;

  static {
    List<String> baggages =
        IntStream.range(0, 100)
            .mapToObj(
                i ->
                    "key"
                        + i
                        + " = value"
                        + i
                        + ";metaKey"
                        + i
                        + "=\tmetaVal"
                        + i
                        + ",broken)key"
                        + i
                        + "=value")
            .collect(Collectors.toList());
    SMALL_BAGGAGE = Collections.singletonMap("baggage", String.join(",", baggages.subList(0, 5)));
    LARGE_BAGGAGE = Collections.singletonMap("baggage", String.join(",", baggages));
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

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(3)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Context smallBaggage() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    return propagator.extract(Context.root(), SMALL_BAGGAGE, getter);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(3)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Context largeBaggage() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    return propagator.extract(Context.root(), LARGE_BAGGAGE, getter);
  }
}
