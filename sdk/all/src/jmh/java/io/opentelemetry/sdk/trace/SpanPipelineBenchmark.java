/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributeKey.booleanKey;
import static io.opentelemetry.common.AttributeKey.doubleKey;
import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringKey;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.StatusCanonicalCode;
import io.opentelemetry.trace.Tracer;
import java.util.Collection;
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

@State(Scope.Benchmark)
public class SpanPipelineBenchmark {

  private static final AttributeKey<String> OPERATION_KEY = stringKey("operation");
  private static final AttributeKey<Long> LONG_ATTRIBUTE_KEY = longKey("longAttribute");
  private static final AttributeKey<String> STRING_ATTRIBUTE_KEY = stringKey("stringAttribute");
  private static final AttributeKey<Double> DOUBLE_ATTRIBUTE_KEY = doubleKey("doubleAttribute");
  private static final AttributeKey<Boolean> BOOLEAN_ATTRIBUTE_KEY = booleanKey("booleanAttribute");
  private final Tracer tracer = OpenTelemetry.getTracerProvider().get("benchmarkTracer");

  @Setup(Level.Trial)
  public final void setup() {
    SpanExporter exporter = new NoOpSpanExporter();
    OpenTelemetrySdk.getTracerManagement()
        .addSpanProcessor(SimpleSpanProcessor.newBuilder(exporter).build());
  }

  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void runThePipeline_05Threads() {
    doWork();
  }

  private void doWork() {
    Span span =
        tracer
            .spanBuilder("benchmarkSpan")
            .setSpanKind(Kind.CLIENT)
            .setAttribute("key", "value")
            .startSpan();
    span.addEvent("started", Attributes.of(OPERATION_KEY, "some_work"));
    span.setAttribute(LONG_ATTRIBUTE_KEY, 33L);
    span.setAttribute(STRING_ATTRIBUTE_KEY, "test_value");
    span.setAttribute(DOUBLE_ATTRIBUTE_KEY, 4844.44d);
    span.setAttribute(BOOLEAN_ATTRIBUTE_KEY, false);
    span.setStatus(StatusCanonicalCode.OK);

    span.addEvent("testEvent");
    span.end();
  }

  private static class NoOpSpanExporter implements SpanExporter {
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      // no-op
      return CompletableResultCode.ofSuccess();
    }
  }
}
