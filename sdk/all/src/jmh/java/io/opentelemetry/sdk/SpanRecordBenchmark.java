/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * This benchmark measures the performance of recording spans and includes the following dimensions:
 *
 * <ul>
 *   <li>{@link BenchmarkState#spanSize}: the size of the span, which is a composite of the number
 *       of attributes, events, and links attached to the span.
 * </ul>
 *
 * <p>Each operation consists of recording {@link SpanRecordBenchmark#RECORDS_PER_INVOCATION} spans.
 *
 * <p>In order to isolate the record path while remaining realistic, the benchmark uses a {@link
 * BatchSpanProcessor} paired with a noop {@link SpanExporter}. In order to avoid quickly outpacing
 * the batch processor queue and dropping spans, the processor is configured with a queue size of
 * {@link SpanRecordBenchmark#RECORDS_PER_INVOCATION} * {@link SpanRecordBenchmark#MAX_THREADS} and
 * is flushed after each invocation.
 */
public class SpanRecordBenchmark {

  private static final int RECORDS_PER_INVOCATION = BenchmarkUtils.RECORDS_PER_INVOCATION;
  private static final int MAX_THREADS = 4;
  private static final int QUEUE_SIZE = RECORDS_PER_INVOCATION * MAX_THREADS;

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    // Encode a variety of dimensions (# attributes, # events, # links) into a single enum to
    // benchmark various shapes of spans without combinatorial explosion.
    @Param SpanSize spanSize;

    SdkTracerProvider tracerProvider;
    Tracer tracer;
    List<AttributeKey<String>> attributeKeys;
    List<String> attributeValues;
    List<Exception> exceptions;
    List<SpanContext> linkContexts;

    @Setup
    public void setup() {
      tracerProvider =
          SdkTracerProvider.builder()
              // Configure a batch processor with a noop exporter (SpanExporter.composite() is a
              // shortcut for a noop exporter). This allows testing the throughput / performance
              // impact of BatchSpanProcessor, which is essential for real workloads, while avoiding
              // noise from SpanExporters whose performance is subject to implementation and network
              // details.
              .addSpanProcessor(
                  BatchSpanProcessor.builder(SpanExporter.composite())
                      .setMaxQueueSize(QUEUE_SIZE)
                      .build())
              .setSampler(Sampler.alwaysOn())
              .build();
      tracer = tracerProvider.get("benchmarkTracer");

      attributeKeys = new ArrayList<>(spanSize.attributes);
      attributeValues = new ArrayList<>(spanSize.attributes);
      for (int i = 0; i < spanSize.attributes; i++) {
        attributeKeys.add(AttributeKey.stringKey("key" + i));
        attributeValues.add("value" + i);
      }

      exceptions = new ArrayList<>(spanSize.events);
      for (int i = 0; i < spanSize.events; i++) {
        exceptions.add(new Exception("test exception"));
      }

      linkContexts = new ArrayList<>(spanSize.links);
      for (int i = 0; i < spanSize.links; i++) {
        linkContexts.add(
            SpanContext.create(
                IdGenerator.random().generateTraceId(),
                IdGenerator.random().generateSpanId(),
                TraceFlags.getDefault(),
                TraceState.getDefault()));
      }
    }

    @TearDown(Level.Invocation)
    public void flush() {
      tracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
    }

    @TearDown
    public void tearDown() {
      tracerProvider.shutdown();
    }
  }

  @Benchmark
  @Group("threads1")
  @GroupThreads(1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  public void record_SingleThread(BenchmarkState benchmarkState) {
    record(benchmarkState);
  }

  @Benchmark
  @Group("threads" + MAX_THREADS)
  @GroupThreads(MAX_THREADS)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  public void record_MultipleThreads(BenchmarkState benchmarkState) {
    record(benchmarkState);
  }

  private static void record(BenchmarkState benchmarkState) {
    for (int i = 0; i < RECORDS_PER_INVOCATION; i++) {
      Span span = benchmarkState.tracer.spanBuilder("test span name").startSpan();
      for (int j = 0; j < benchmarkState.attributeKeys.size(); j++) {
        span.setAttribute(
            benchmarkState.attributeKeys.get(j), benchmarkState.attributeValues.get(j));
      }
      for (int j = 0; j < benchmarkState.exceptions.size(); j++) {
        span.recordException(benchmarkState.exceptions.get(j));
      }
      for (int j = 0; j < benchmarkState.linkContexts.size(); j++) {
        span.addLink(benchmarkState.linkContexts.get(j));
      }
      span.end();
    }
  }

  public enum SpanSize {
    SMALL(0, 0, 0),
    MEDIUM(10, 1, 0),
    LARGE(100, 10, 5);

    private final int attributes;
    private final int events;
    private final int links;

    SpanSize(int attributes, int events, int links) {
      this.attributes = attributes;
      this.events = events;
      this.links = links;
    }
  }
}
