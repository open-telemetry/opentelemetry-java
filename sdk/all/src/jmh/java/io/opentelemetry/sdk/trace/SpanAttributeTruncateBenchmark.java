/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.config.TraceConfig;
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

  private final Tracer tracerSdk = OpenTelemetry.getGlobalTracer("benchmarkTracer");
  private SpanBuilderSdk spanBuilderSdk;

  public final String shortValue = "short";
  public final String longValue = "very_long_attribute_and_then_some_more";
  public String veryLongValue;

  @Param({"10", "1000000"})
  public int maxLength;

  @Setup(Level.Trial)
  public final void setup() {
    TraceConfig config =
        OpenTelemetrySdk.getGlobalTracerManagement().getActiveTraceConfig().toBuilder()
            .setMaxLengthOfAttributeValues(maxLength)
            .build();
    OpenTelemetrySdk.getGlobalTracerManagement().updateActiveTraceConfig(config);
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
