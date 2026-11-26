/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.ThreadParams;

/** Common resource ownership and complete-iteration accounting for batch processor benchmarks. */
@State(Scope.Benchmark)
public class BatchSpanProcessorBenchmarkState implements AutoCloseable {
  @Param({"0"})
  public int delayMs;

  @Param({"2048"})
  public int maxQueueSize = 2048;

  @Param({"512"})
  public int maxExportBatchSize = 512;

  private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
  @Nullable private volatile Thread exporterThread;
  double exporterCpuNanos;
  private final Logger logger = Logger.getLogger(BatchSpanProcessor.class.getName());
  private final Queue<ProducerState> producers = new ConcurrentLinkedQueue<>();
  @Nullable private Level previousLogLevel;
  @Nullable private SdkTracerProvider tracerProvider;
  @Nullable private SdkMeterProvider meterProvider;
  private InMemoryMetricReader metricReader;
  Tracer tracer;
  long attemptedSpans;
  long exportedSpans;
  long droppedSpans;
  long elapsedNanos;
  boolean requireNoDrops;
  private long startedNanos;

  @Setup(org.openjdk.jmh.annotations.Level.Trial)
  public void configureLogging() {
    // Per-batch queue-full warnings would otherwise add console I/O to the exporter hot path.
    previousLogLevel = logger.getLevel();
    logger.setLevel(Level.OFF);
  }

  @Setup(org.openjdk.jmh.annotations.Level.Iteration)
  public void setup() {
    metricReader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
    // A zero-delay exporter measures the processor without an unrelated executor handoff.
    exporterThread = null;
    SpanExporter delegate = delayMs == 0 ? SpanExporter.noop() : new DelayingSpanExporter(delayMs);
    SpanExporter exporter =
        new SpanExporter() {
          @Override
          public CompletableResultCode export(Collection<SpanData> spans) {
            if (exporterThread == null) {
              exporterThread = Thread.currentThread();
            }
            return delegate.export(spans);
          }

          @Override
          public CompletableResultCode flush() {
            return delegate.flush();
          }

          @Override
          public CompletableResultCode shutdown() {
            return delegate.shutdown();
          }
        };
    BatchSpanProcessor processor =
        BatchSpanProcessor.builder(exporter)
            .setMeterProvider(meterProvider)
            .setInternalTelemetryVersion(InternalTelemetryVersion.LEGACY)
            .setMaxQueueSize(maxQueueSize)
            .setMaxExportBatchSize(maxExportBatchSize)
            .build();
    tracerProvider = SdkTracerProvider.builder().addSpanProcessor(processor).build();
    tracer = tracerProvider.get("benchmarkTracer");
    startedNanos = System.nanoTime();
  }

  void endSpan(ProducerState producer) {
    producer.attemptedSpans++;
    tracer.spanBuilder("span").startSpan().end();
  }

  @SuppressWarnings("SystemOut") // One accounting record after timing, including SampleTime runs.
  @TearDown(org.openjdk.jmh.annotations.Level.Iteration)
  public void finishIteration() {
    try {
      flush();
      elapsedNanos = System.nanoTime() - startedNanos;
      BatchSpanProcessorMetrics metrics =
          new BatchSpanProcessorMetrics(metricReader.collectAllMetrics());
      exportedSpans = metrics.exportedSpans();
      droppedSpans = metrics.droppedSpans();
      // JMH's pre-teardown barrier has stopped every producer before this shared fixture runs.
      attemptedSpans = producers.stream().mapToLong(producer -> producer.attemptedSpans).sum();
      if (attemptedSpans != exportedSpans + droppedSpans) {
        throw new IllegalStateException(
            "Incomplete benchmark accounting: attempted="
                + attemptedSpans
                + ", exported="
                + exportedSpans
                + ", dropped="
                + droppedSpans);
      }
      if (requireNoDrops && droppedSpans != 0) {
        throw new IllegalStateException("Latency workload overloaded the queue: " + droppedSpans);
      }
      exporterCpuNanos = Double.NaN;
      if (threads.isThreadCpuTimeSupported() && threads.isThreadCpuTimeEnabled()) {
        Thread worker = exporterThread;
        long cpu = worker == null ? -1 : threads.getThreadCpuTime(worker.getId());
        if (cpu >= 0) {
          exporterCpuNanos = cpu;
        }
      }
      long skippedSlots = producers.stream().mapToLong(producer -> producer.skippedSlots).sum();
      System.out.printf(
          "BSP_LEDGER attempted=%d exported=%d dropped=%d elapsedNanos=%d skippedSlots=%d exporterCpuNanos=%.0f%n",
          attemptedSpans,
          exportedSpans,
          droppedSpans,
          elapsedNanos,
          skippedSlots,
          exporterCpuNanos);
    } finally {
      close();
    }
  }

  void flush() {
    if (tracerProvider == null) {
      throw new IllegalStateException("Tracer provider was not initialized");
    }
    CompletableResultCode flush = tracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
    if (!flush.isSuccess()) {
      throw new IllegalStateException("Benchmark queue did not finish draining");
    }
  }

  @SuppressWarnings(
      "Interruption") // This fixture owns the captured SDK worker and has shut it down.
  @Override
  public void close() {
    try {
      if (tracerProvider != null) {
        tracerProvider.close();
        tracerProvider = null;
      }
      Thread worker = exporterThread;
      if (worker != null) {
        // Older processors can finish shutdown before leaving their timed wait. Do not let an
        // iteration's worker overlap the next iteration, even when benchmarking that baseline.
        worker.interrupt();
        try {
          worker.join(10_000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("Interrupted while stopping benchmark worker", e);
        }
        if (worker.isAlive()) {
          throw new IllegalStateException("Benchmark worker did not stop");
        }
      }
    } finally {
      if (meterProvider != null) {
        meterProvider.close();
        meterProvider = null;
      }
    }
  }

  @TearDown(org.openjdk.jmh.annotations.Level.Trial)
  public void restoreLogging() {
    logger.setLevel(previousLogLevel);
  }

  @State(Scope.Thread)
  public static class ProducerState {
    long attemptedSpans;
    long skippedSlots;
    BatchSpanProcessorBenchmarkState benchmarkState;
    boolean publishesTotals;

    void register(BatchSpanProcessorBenchmarkState state, int threadIndex) {
      benchmarkState = state;
      publishesTotals = threadIndex == 0;
      state.producers.add(this);
    }

    @Setup(org.openjdk.jmh.annotations.Level.Iteration)
    public void reset() {
      attemptedSpans = 0;
      skippedSlots = 0;
    }
  }

  /** Raw counts are additive; ratios must be computed from the aggregated counts, never summed. */
  @State(Scope.Thread)
  @AuxCounters(AuxCounters.Type.EVENTS)
  public static class Counters extends ProducerState {
    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setup(BatchSpanProcessorBenchmarkState state, ThreadParams params) {
      register(state, params.getThreadIndex());
    }

    public long attemptedSpans() {
      return attemptedSpans;
    }

    public long exportedSpans() {
      return publishesTotals ? benchmarkState.exportedSpans : 0;
    }

    public long droppedSpans() {
      return publishesTotals ? benchmarkState.droppedSpans : 0;
    }

    public double exporterCpuNanos() {
      return publishesTotals ? benchmarkState.exporterCpuNanos : 0;
    }

    public long elapsedNanos() {
      return publishesTotals ? benchmarkState.elapsedNanos : 0;
    }
  }
}
