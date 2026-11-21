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
import org.openjdk.jmh.infra.BenchmarkParams;

/**
 * Measures how many spans {@link BatchSpanProcessor} drops under two load shapes with the same
 * average rate ({@code offeredLoad} spans/s).
 *
 * <ul>
 *   <li>{@code STEADY}: one span every {@code 1s / offeredLoad}.
 *   <li>{@code BURSTY}: 6,000 spans 5 µs apart (30 ms), then idle; bursts start every {@code 6000 /
 *       offeredLoad} seconds.
 * </ul>
 *
 * <p>Each operation produces {@code offeredLoad * 10} spans on that schedule without flushing in
 * between, so queue and exporter backlog carry across bursts, then calls {@code forceFlush()} so
 * the accounting can be checked. The exporter completes asynchronously after {@code
 * exporterLatencyMs}. The producer busy-waits, so it occupies a core.
 *
 * <p>The result to read is {@code droppedFraction}. JMH adds aux counters across measured
 * iterations, so each iteration contributes its fraction divided by the iteration count and the
 * summary line is the mean. {@code ms/op} only reflects the schedule, and {@code maxLagMicros}
 * shows how far the producer fell behind it (reported, not enforced).
 *
 * <p>Motivation: https://github.com/open-telemetry/opentelemetry-java/issues/7508
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
// An operation takes about WINDOW_SECONDS, which is longer than the iteration time, so JMH runs
// exactly one operation per iteration.
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 1)
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

  /**
   * The producer sleeps only until this long before a target, then spins. It must exceed how late a
   * sleep can wake up; on Windows that can be a full timer tick (about 15.6 ms), so 20 ms leaves
   * headroom. The cost is up to this much spinning per gap.
   */
  private static final long SPIN_MARGIN_NANOS = 20_000_000;

  @Param({"STEADY", "BURSTY"})
  Scenario scenario;

  /** Average spans per second, the same for both scenarios. The issue reports about 15,000. */
  @Param({"7500", "15000"})
  int offeredLoad;

  /** 2048 is the BSP default; 8192 shows the effect of a larger queue. */
  @Param({"2048", "8192"})
  int maxQueueSize;

  /** Simulated time an export takes to complete (e.g. a network round trip). */
  @Param({"10", "50", "200"})
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

  // Number of measured iterations, used to scale droppedFraction (see LoadCounters).
  private int measuredIterations;

  // Cumulative values last seen in the processor's metrics, used to compute per-operation deltas.
  private long lastDropped;
  private long lastHandedToExporter;

  @Setup(Level.Trial)
  public void setUp(BenchmarkParams benchmarkParams) {
    // Effective values, so command line overrides of iterations or forks are respected. JMH reports
    // a forks value of 0 when running in-process, which still produces one set of iterations.
    measuredIterations =
        benchmarkParams.getMeasurement().getCount() * Math.max(1, benchmarkParams.getForks());

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

    /**
     * Share of produced spans that were dropped. JMH adds aux counters across measured iterations,
     * so each iteration contributes its fraction divided by the number of iterations, which makes
     * the summary line the mean. Per-iteration values are therefore 1/N of the true fraction; read
     * the summary line, or divide droppedSpans by producedSpans.
     */
    public double droppedFraction;

    /**
     * Worst time the producer was behind its schedule during an iteration, in microseconds. This is
     * a max, but JMH's summary score adds the iterations together, so read the per-iteration values
     * from the raw data.
     */
    public long maxLagMicros;

    /** Worst lag at the first span of a burst (a late wake-up), in microseconds. Same caveat. */
    public long maxBurstStartLagMicros;

    @Setup(Level.Iteration)
    public void reset() {
      producedSpans = 0;
      exportedSpans = 0;
      droppedSpans = 0;
      droppedFraction = 0;
      maxLagMicros = 0;
      maxBurstStartLagMicros = 0;
    }
  }

  @Benchmark
  public void load(LoadCounters counters) {
    long maxLagNanos = 0;
    long maxBurstStartLagNanos = 0;
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
        if (j == 0) {
          maxBurstStartLagNanos = Math.max(maxBurstStartLagNanos, lagNanos);
        }

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
    counters.droppedFraction += (double) dropped / totalSpans / measuredIterations;
    counters.maxLagMicros =
        Math.max(counters.maxLagMicros, TimeUnit.NANOSECONDS.toMicros(maxLagNanos));
    counters.maxBurstStartLagMicros =
        Math.max(
            counters.maxBurstStartLagMicros, TimeUnit.NANOSECONDS.toMicros(maxBurstStartLagNanos));
  }

  /**
   * Blocks until {@code targetNanos} (a {@link System#nanoTime()} value) and returns how far past
   * it the clock was when waiting stopped (the lag). Never skips: when already behind schedule it
   * returns immediately, so the caller emits late rather than dropping spans, and the lateness
   * shows up in the return value.
   */
  private static long waitUntil(long targetNanos) {
    long remaining;
    while ((remaining = targetNanos - System.nanoTime()) > 0) {
      if (remaining > SPIN_MARGIN_NANOS) {
        // Sleep until SPIN_MARGIN_NANOS before the target, then spin, so that an oversleep of up to
        // the margin does not make the producer late.
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
