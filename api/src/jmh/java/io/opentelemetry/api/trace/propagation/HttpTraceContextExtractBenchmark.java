/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator.Getter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
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
public class HttpTraceContextExtractBenchmark {

  private static final String TRACEPARENT = "traceparent";
  private static final int COUNT = 5;
  private static final List<String> traceparentsHeaders =
      Arrays.asList(
          "00-905734c59b913b4a905734c59b913b4a-9909983295041501-01",
          "00-21196a77f299580e21196a77f299580e-993a97ee3691eb26-00",
          "00-2e7d0ad2390617702e7d0ad239061770-d49582a2de984b86-01",
          "00-905734c59b913b4a905734c59b913b4a-776ff807b787538a-00",
          "00-68ec932c33b3f2ee68ec932c33b3f2ee-68ec932c33b3f2ee-00");
  private final HttpTraceContext httpTraceContext = HttpTraceContext.getInstance();
  private final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Override
        public Collection<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };
  private static final List<Map<String, String>> carriers =
      getCarrierForHeader(traceparentsHeaders);

  /** Benchmark for measuring HttpTraceContext extract. */
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  @OperationsPerInvocation(COUNT)
  @Nullable
  public Context measureExtract() {
    Context result = null;
    for (int i = 0; i < COUNT; i++) {
      result = httpTraceContext.extract(Context.root(), carriers.get(i), getter);
    }
    return result;
  }

  private static List<Map<String, String>> getCarrierForHeader(List<String> headers) {
    List<Map<String, String>> carriers = new ArrayList<>();
    for (String header : headers) {
      Map<String, String> carrier = new HashMap<>();
      carrier.put(TRACEPARENT, header);
      carriers.add(carrier);
    }
    return carriers;
  }
}
