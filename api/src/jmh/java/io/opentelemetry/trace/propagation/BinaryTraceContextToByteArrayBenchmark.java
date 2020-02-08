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

import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class BinaryTraceContextToByteArrayBenchmark {

  private BinaryTraceContext binaryTraceContext;

  @State(Scope.Thread)
  public static class BinaryTraceContextToByteArrayState {

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

    public SpanContext spanContext;

    private byte sampledTraceOptionsBytes = 1;
    private TraceFlags sampledTraceOptions = TraceFlags.fromByte(sampledTraceOptionsBytes);
    private TraceState traceStateDefault = TraceState.builder().build();

    @Setup
    public void setup() {
      this.spanContext =
          SpanContext.create(
              TraceId.fromLowerBase16(traceIdBase16, 0),
              SpanId.fromLowerBase16(spanIdBase16, 0),
              sampledTraceOptions,
              traceStateDefault);
    }
  }

  @Setup
  public void setup() {
    this.binaryTraceContext = new BinaryTraceContext();
  }

  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
  public byte[] measureToByteArray(BinaryTraceContextToByteArrayState state) {
    return binaryTraceContext.toByteArray(state.spanContext);
  }
}
