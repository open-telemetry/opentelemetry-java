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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span.Kind;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class SpanAttributeTruncateBenchmark {

  private final TracerSdk tracerSdk = OpenTelemetrySdk.getTracerProvider().get("benchmarkTracer");
  private SpanBuilderSdk spanBuilderSdk;

  public String shortValue = "short";
  public String longValue = "very_long_attribute_and_then_some_more";
  public String veryLongValue;

  @Param({"10", "1000000"})
  public int maxLength;

  @Setup(Level.Trial)
  public final void setup() {
    TraceConfig config =
        OpenTelemetrySdk.getTracerProvider()
            .getActiveTraceConfig()
            .toBuilder()
            .setMaxLengthOfAttributeValues(maxLength)
            .build();
    OpenTelemetrySdk.getTracerProvider().updateActiveTraceConfig(config);
    spanBuilderSdk =
        (SpanBuilderSdk)
            tracerSdk
                .spanBuilder("benchmarkSpan")
                .setSpanKind(Kind.CLIENT)
                .setAttribute("key", "value");

    String seed = "0123456789";
    StringBuilder longString = new StringBuilder();
    while (longString.length() < 10_000_000) {
      longString.append(seed);
    }
    veryLongValue = longString.toString();
  }

  /** attributes that don't require any truncation. */
  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public RecordEventsReadableSpan shortAttributes() {
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilderSdk.startSpan();
    for (int i = 0; i < 10; i++) {
      span.setAttribute(String.valueOf(i), shortValue);
    }
    return span;
  }

  /** even if we truncate, result is short. */
  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public RecordEventsReadableSpan longAttributes() {
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilderSdk.startSpan();
    for (int i = 0; i < 10; i++) {
      span.setAttribute(String.valueOf(i), longValue);
    }
    return span;
  }

  /** have to copy very long strings. */
  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public RecordEventsReadableSpan veryLongAttributes() {
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilderSdk.startSpan();
    for (int i = 0; i < 10; i++) {
      span.setAttribute(String.valueOf(i), veryLongValue);
    }
    return span;
  }
}
