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
import java.util.concurrent.locks.LockSupport;
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
 * Measures span loss in {@link BatchSpanProcessor} under two defined load shapes that have the same
 * average rate ({@code offeredLoad} spans per second) but different arrival patterns:
 *
 * <ul>
 *   <li>{@code STEADY}: one span every {@code 1s / offeredLoad}.
 *   <li>{@code BURSTY}: {@value #BURST_SIZE} spans, {@value #BURST_SPAN_INTERVAL_NANOS} ns apart
 *       (30 ms per burst), then idle until the next burst. Bursts start every {@code BURST_SIZE /
 *       offeredLoad} seconds, which is 400 ms at 15,000 spans/s. The last burst is partial if
 *       {@code offeredLoad * WINDOW_SECONDS} is not a multiple of the burst size, so the bursty
 *       window ends at its last burst rather than at the full {@value #WINDOW_SECONDS} seconds.
 * </ul>
 *
 * <p>Each operation produces exactly {@code offeredLoad * WINDOW_SECONDS} spans on a schedule, with
 * no flushing in between, so the queue and exporter backlog evolve naturally. Production happens in
 * a single loop shared by both scenarios, which only differ in burst size and spacing. After the
 * last span is produced, {@code forceFlush()} drains the accepted spans so the accounting can be
 * verified and the next operation starts from an empty queue. Drops are decided at enqueue time, so
 * the flush does not affect them.
 *
 * <p>The producer busy-waits for short gaps because no sleep call is precise enough for microsecond
 * spacing, so it occupies one core while running. The producer's worst lag behind its schedule is
 * reported as {@code maxLagMicros}; it is only reported, not enforced. A large value means the
 * traffic pattern was not reproduced faithfully.
 *
 * <p>{@code ms/op} just reflects the production window and the final drain. The result to look at
 * is the share of produced spans that were dropped.
 *
 * <p>Processor telemetry is compared with an independent exporter counter to verify that every
 * produced span is either exported or dropped.
 *
 * <p>Aux counters use {@link AuxCounters.Type#EVENTS}, so JMH reports totals per iteration. Ratios
 * should be calculated from the totals.
 *
 * <p>Motivation: https://github.com/open-telemetry/opentelemetry-java/issues/7508
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
// An operation takes about WINDOW_SECONDS, which is longer than the iteration time, so JMH runs
// exactly one operation per iteration.
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 3, time = 1)
@State(Scope.Benchmark)
public class BatchSpanProcessorBurstBenchmark {

  /** The load shapes. */
  public enum Scenario {
    STEADY,
    BURSTY
  }

  /** This benchmark produces spans from a single thread. */
  private static final int SINGLE_THREAD = 1;

  /** The issue reports bursts of 5,000-6,000 spans within 30 ms. */
  private static final int BURST_SIZE = 6000;

  /** 6,000 spans * 5 microseconds = 30 ms. */
  private static final long BURST_SPAN_INTERVAL_NANOS = 5_000;

  /** Length of the production window. */
  private static final int WINDOW_SECONDS = 10;

  private static final int MAX_EXPORT_BATCH_SIZE = 512;
  private static final long NANOS_PER_SECOND = 1_000_000_000L;

  /** Gaps longer than this are partly slept; shorter ones are spun. */
  private static final long PARK_THRESHOLD_NANOS = 2_000_000;

  /** How long before the target to wake up when sleeping, leaving the rest to spinning. */
  private static final long SPIN_MARGIN_NANOS = 1_000_000;

  @Param({"STEADY", "BURSTY"})
  Scenario scenario;

  /** Average spans per second, the same for both scenarios. The issue reports about 15,000. */
    @Param({"7500", "15000"})
  // @Param({"15000"})
  int offeredLoad;

  /** 2048 is the BSP default; 8192 shows the effect of a larger queue. */
  @Param({"2048", "8192"})
  // @Param({"2048"})
  int maxQueueSize;

  /** Simulated time an export takes to complete (e.g. a network round trip). */
  @Param({"10", "50", "200"})
  // @Param("50")
  int exporterLatencyMs;

  private ScheduledExecutorService scheduler;
  private LatencyCountingExporter exporter;
  private InMemoryMetricReader metricReader;
  private SdkMeterProvider meterProvider;
  private SdkTracerProvider tracerProvider;
  private BatchSpanProcessor processor;
  private Tracer tracer;

  // The schedule, derived from the scenario. A steady load is a degenerate burst of one span.
  private int burstSize;
  private long spanIntervalNanos;
  private long totalSpans;

  // Cumulative values last seen in the processor's metrics, used to compute per-operation deltas.
  private long lastDropped;
  private long lastHandedToExporter;

  @Setup(Level.Trial)
  public void setUp() {
    switch (scenario) {
      case STEADY:
        burstSize = 1;
        spanIntervalNanos = 0;
        break;
      case BURSTY:
        burstSize = BURST_SIZE;
        spanIntervalNanos = BURST_SPAN_INTERVAL_NANOS;
        break;
    }
    totalSpans = (long) offeredLoad * WINDOW_SECONDS;

    scheduler = Executors.newSingleThreadScheduledExecutor();
    exporter = new LatencyCountingExporter(scheduler, exporterLatencyMs);
    metricReader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
    processor =
        BatchSpanProcessor.builder(exporter)
            .setMeterProvider(meterProvider)
            .setMaxQueueSize(maxQueueSize)
            .setMaxExportBatchSize(MAX_EXPORT_BATCH_SIZE)
            // Long delay so exports are triggered by batch size / flush, not the timer.
            .setScheduleDelay(Duration.ofSeconds(30))
            .build();
    tracerProvider = SdkTracerProvider.builder().addSpanProcessor(processor).build();
    tracer = tracerProvider.get("load-benchmark");
    lastDropped = 0;
    lastHandedToExporter = 0;
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    tracerProvider.shutdown().join(10, TimeUnit.SECONDS);
    meterProvider.shutdown().join(10, TimeUnit.SECONDS);
    scheduler.shutdownNow();
  }

  /** Per-iteration totals for produced, exported, and dropped spans, plus the producer's lag. */
  @AuxCounters(AuxCounters.Type.EVENTS)
  @State(Scope.Thread)
  public static class LoadCounters {
    public long producedSpans;

    /** Spans whose export completed, as counted by the benchmark's own exporter. */
    public long exportedSpans;

    /** Spans the processor reports as dropped because the queue was full. */
    public long droppedSpans;

    /** Worst time the producer was behind its schedule, in microseconds. Not a total. */
    public long maxLagMicros;

    @Setup(Level.Iteration)
    public void reset() {
      producedSpans = 0;
      exportedSpans = 0;
      droppedSpans = 0;
      maxLagMicros = 0;
    }
  }

  @Benchmark
  public void load(LoadCounters counters) {
    long maxLagNanos = 0;
    long spansProduced = 0;
    long startNanos = System.nanoTime();

    for (int burst = 0; spansProduced < totalSpans; burst++) {
      // Bursts start every burstSize / offeredLoad seconds, so the average rate is offeredLoad for
      // any burst size. Computed from the burst index, not by adding a rounded period, so that
      // integer rounding does not accumulate. The cast keeps the multiplication in long.
      long burstStart = startNanos + (long) burst * burstSize * NANOS_PER_SECOND / offeredLoad;

      // The last burst may be partial so exactly totalSpans are produced.
      long spansThisBurst = Math.min(burstSize, totalSpans - spansProduced);

      for (int j = 0; j < spansThisBurst; j++) {
        long lagNanos = waitUntil(burstStart + j * spanIntervalNanos);
        maxLagNanos = Math.max(maxLagNanos, lagNanos);

        tracer.spanBuilder("load-span").startSpan().end();
        spansProduced++;
      }
    }

    // Production is over, so drops are final. Wait for all accepted spans to finish exporting.
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
    if (handedToExporter + dropped != totalSpans || handedToExporter != completed) {
      throw new IllegalStateException(
          "Span accounting mismatch: totalSpans="
              + totalSpans
              + " handedToExporter="
              + handedToExporter
              + " dropped="
              + dropped
              + " completed="
              + completed);
    }

    counters.producedSpans += totalSpans;
    counters.exportedSpans += completed;
    counters.droppedSpans += dropped;
    counters.maxLagMicros =
        Math.max(counters.maxLagMicros, TimeUnit.NANOSECONDS.toMicros(maxLagNanos));
  }

  /**
   * Blocks until {@code targetNanos} (a {@link System#nanoTime()} value) and returns how far past
   * it the clock was when waiting stopped (the lag). Never skips: when already behind schedule it
   * returns immediately, so the caller emits late rather than dropping spans, and the lateness
   * shows up in the return value.
   */
  private static long waitUntil(long targetNanos) {
    long remaining;
    while ((remaining = targetNanos - System.nanoTime()) > 0) { // busy waiting
      if (remaining > PARK_THRESHOLD_NANOS) {
        // Sleep most of the gap, then spin for the rest to avoid oversleeping.
        LockSupport.parkNanos(remaining - SPIN_MARGIN_NANOS);
      }
      // Otherwise spin: re-read the clock until the target is reached.
    }
    return -remaining;
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
