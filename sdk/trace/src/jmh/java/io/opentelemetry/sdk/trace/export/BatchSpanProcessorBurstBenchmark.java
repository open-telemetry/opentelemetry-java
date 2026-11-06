/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures span loss when {@link BatchSpanProcessor} processes large bursts.
 *
 * <p>Each operation produces one burst and calls {@code forceFlush()}, ensuring the queue is empty
 * before the next burst.
 *
 * <p>Processor telemetry is compared with an independent exporter counter to verify that every
 * produced span is either exported or dropped.
 *
 * <p>Aux counters use {@link AuxCounters.Type#EVENTS}, so JMH reports totals per iteration.
 * Per-burst values can be calculated from the total number of bursts, and ratios should be
 * calculated from the totals.
 *
 * <p>Motivation: https://github.com/open-telemetry/opentelemetry-java/issues/7508
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@State(Scope.Benchmark)
public class BatchSpanProcessorBurstBenchmark {

  /** This benchmark produces spans from a single thread. */
  private static final int SINGLE_THREAD = 1;

  /** Spans produced back-to-back per operation. The issue reports bursts of 5,000-6,000. */
  @Param({"6000"})
  int burstSize;

  /** 2048 is the BSP default; 8192 shows the effect of a larger queue. */
  @Param({"2048", "8192"})
  int maxQueueSize;

  @Param({"512"})
  int maxExportBatchSize;

  /** Simulated time an export takes to complete (e.g. a network round trip). */
  @Param({"50"})
  int exporterLatencyMs;

  private ScheduledExecutorService scheduler;
  private LatencyCountingExporter exporter;
  private InMemoryMetricReader metricReader;
  private SdkMeterProvider meterProvider;
  private SdkTracerProvider tracerProvider;
  private BatchSpanProcessor processor;
  private Tracer tracer;

  // Cumulative values last seen in the processor's metrics, used to compute per-operation deltas.
  private long lastDropped;
  private long lastHandedToExporter;

  @Setup(Level.Trial)
  public void setUp() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    exporter = new LatencyCountingExporter(scheduler, exporterLatencyMs);
    metricReader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
    processor =
        BatchSpanProcessor.builder(exporter)
            .setMeterProvider(meterProvider)
            .setMaxQueueSize(maxQueueSize)
            .setMaxExportBatchSize(maxExportBatchSize)
            // Long delay so exports are triggered by batch size / flush, not the timer.
            .setScheduleDelay(Duration.ofSeconds(30))
            .build();
    tracerProvider = SdkTracerProvider.builder().addSpanProcessor(processor).build();
    tracer = tracerProvider.get("burst-benchmark");
    lastDropped = 0;
    lastHandedToExporter = 0;
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    tracerProvider.shutdown().join(10, TimeUnit.SECONDS);
    meterProvider.shutdown().join(10, TimeUnit.SECONDS);
    scheduler.shutdownNow();
  }

  /** Per-iteration totals for produced, exported, and dropped spans. */
  @AuxCounters(AuxCounters.Type.EVENTS)
  @State(Scope.Thread)
  public static class BurstCounters {
    public long producedSpans;

    /** Spans whose export completed, as counted by the benchmark's own exporter. */
    public long exportedSpans;

    /** Spans the processor reports as dropped because the queue was full. */
    public long droppedSpans;

    @Setup(Level.Iteration)
    public void reset() {
      producedSpans = 0;
      exportedSpans = 0;
      droppedSpans = 0;
    }
  }

  @Benchmark
  public void burst(BurstCounters counters) {
    for (int i = 0; i < burstSize; i++) {
      tracer.spanBuilder("burst-span").startSpan().end();
    }

    // Wait for all accepted spans to finish exporting.
    CompletableResultCode flushResult = processor.forceFlush().join(30, TimeUnit.SECONDS);
    if (!flushResult.isSuccess()) {
      throw new IllegalStateException("forceFlush did not complete successfully within 30s");
    }

    // Collect exporter and processor counts for this operation.
    long completed = exporter.completedSpans.getAndSet(0);
    BatchSpanProcessorMetrics metrics =
        new BatchSpanProcessorMetrics(metricReader.collectAllMetrics(), SINGLE_THREAD);
    // The metrics are cumulative, so subtract what was seen after the previous operation.
    long handedToExporter = metrics.exportedSpans() - lastHandedToExporter;
    long dropped = metrics.droppedSpans() - lastDropped;
    lastHandedToExporter = metrics.exportedSpans();
    lastDropped = metrics.droppedSpans();

    // Every produced span must be handed to the exporter or dropped, and everything handed to
    // the exporter must have completed. Otherwise the numbers below would be misleading.
    if (handedToExporter + dropped != burstSize || handedToExporter != completed) {
      throw new IllegalStateException(
          "Span accounting mismatch: burstSize="
              + burstSize
              + " handedToExporter="
              + handedToExporter
              + " dropped="
              + dropped
              + " completed="
              + completed);
    }

    counters.producedSpans += burstSize;
    counters.exportedSpans += completed;
    counters.droppedSpans += dropped;
  }

  /** Exporter whose result completes asynchronously after a fixed delay. */
  private static final class LatencyCountingExporter implements SpanExporter {
    final AtomicLong completedSpans = new AtomicLong();
    private final ScheduledExecutorService scheduler;
    private final long latencyMs;

    LatencyCountingExporter(ScheduledExecutorService scheduler, long latencyMs) {
      this.scheduler = scheduler;
      this.latencyMs = latencyMs;
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public CompletableResultCode export(Collection<SpanData> spans) {
      // Capture the size now; the collection may be reused by the caller.
      int size = spans.size();
      CompletableResultCode result = new CompletableResultCode();
      scheduler.schedule(
          () -> {
            completedSpans.addAndGet(size);
            result.succeed();
          },
          latencyMs,
          TimeUnit.MILLISECONDS);
      return result;
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }
}
