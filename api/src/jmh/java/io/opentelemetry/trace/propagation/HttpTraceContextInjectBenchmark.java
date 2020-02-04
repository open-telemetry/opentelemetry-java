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

import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class HttpTraceContextInjectBenchmark {

  private String traceIdBase16 = "ff000000000000000000000000000041";
  private TraceId traceId = TraceId.fromLowerBase16(traceIdBase16, 0);
  private String spanIdBase16 = "ff00000000000041";
  private SpanId spanId = SpanId.fromLowerBase16(spanIdBase16, 0);
  private byte sampledTraceOptionsBytes = 1;
  private TraceFlags sampledTraceOptions = TraceFlags.fromByte(sampledTraceOptionsBytes);
  private Tracestate traceStateDefault = Tracestate.builder().build();

  private HttpTraceContext httpTraceContext;
  private Map<String, String> carrier;
  private Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void set(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };

  @Setup
  public void setup() {
    this.httpTraceContext = new HttpTraceContext();
    this.carrier = new LinkedHashMap<>();
  }

  /**
   * Benchmark for measuring inject with default trace state and sampled trace options.
   */
  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
  public Map<String, String> measureInject() {
    httpTraceContext.inject(
        SpanContext.create(traceId, spanId, sampledTraceOptions, traceStateDefault),
        carrier,
        setter);
    return carrier;
  }

  @TearDown(Level.Iteration)
  public void refreshCarrier() {
    this.carrier = new LinkedHashMap<>();
  }
}
