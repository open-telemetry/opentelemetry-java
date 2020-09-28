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

package io.opentelemetry.sdk.extensions.zpages;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/** Benchmark class for {@link TracezSpanBuckets}. */
@State(Scope.Benchmark)
public class TracezSpanBucketsBenchmark {

  private static final String spanName = "BENCHMARK_SPAN";
  private static ReadableSpan readableSpan;
  private TracezSpanBuckets bucket;

  @Setup(Level.Trial)
  public final void setup() {
    bucket = new TracezSpanBuckets();
    Tracer tracer = OpenTelemetry.getTracer("TracezZPageBenchmark");
    Span span = tracer.spanBuilder(spanName).startSpan();
    span.end();
    readableSpan = (ReadableSpan) span;
  }

  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_01Thread() {
    bucket.addToBucket(readableSpan);
  }

  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_05Threads() {
    bucket.addToBucket(readableSpan);
  }

  @Benchmark
  @Threads(value = 10)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_10Threads() {
    bucket.addToBucket(readableSpan);
  }

  @Benchmark
  @Threads(value = 20)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void addToBucket_20Threads() {
    bucket.addToBucket(readableSpan);
  }
}
