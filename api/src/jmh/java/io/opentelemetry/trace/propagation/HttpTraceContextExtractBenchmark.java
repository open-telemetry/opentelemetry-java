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

package io.opentelemetry.trace.propagation;

import io.opentelemetry.context.propagation.HttpTextFormat.Getter;
import io.opentelemetry.trace.SpanContext;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class HttpTraceContextExtractBenchmark {

  private String traceparent = "traceparent";
  private HttpTraceContext httpTraceContext;
  private Map<String, String> carrier;
  private Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @State(Scope.Thread)
  public static class HttpTraceContextExtractState {

    @Param({
      "905734c59b913b4a905734c59b913b4a",
      "21196a77f299580e21196a77f299580e",
      "2e7d0ad2390617702e7d0ad239061770",
      "905734c59b913b4a905734c59b913b4a",
      "68ec932c33b3f2ee68ec932c33b3f2ee"
    })
    public String traceIdBase16;

    @Param({
      "9909983295041501",
      "993a97ee3691eb26",
      "d49582a2de984b86",
      "776ff807b787538a",
      "68ec932c33b3f2ee"
    })
    public String spanIdBase16;

    public String traceparentHeaderSampled;

    @Setup
    public void setup() {
      this.traceparentHeaderSampled = "00-" + traceIdBase16 + "-" + spanIdBase16 + "-01";
    }
  }

  @Setup
  public void setup(HttpTraceContextExtractState state) {
    this.httpTraceContext = new HttpTraceContext();
    this.carrier = new LinkedHashMap<>();
    this.carrier.put(traceparent, state.traceparentHeaderSampled);
  }

  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
  public SpanContext measureExtract() {
    return httpTraceContext.extract(carrier, getter);
  }

  @TearDown(Level.Iteration)
  public void refreshCarrier(HttpTraceContextExtractState state) {
    this.carrier = new HashMap<>();
    this.carrier.put(traceparent, state.traceparentHeaderSampled);
  }
}
