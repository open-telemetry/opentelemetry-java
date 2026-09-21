/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A {@link MetricReader} which wraps a {@link MetricExporter} and automatically reads and exports
 * the metrics every export interval.
 *
 * <p>Register with {@link SdkMeterProvider} via {@link
 * SdkMeterProviderBuilder#registerMetricReader(MetricReader)}.
 *
 * @since 1.14.0
 */
public final class PeriodicMetricReader implements MetricReader {
  private static final Logger logger = Logger.getLogger(PeriodicMetricReader.class.getName());

  private static final Clock CLOCK = Clock.getDefault();

  private static final ComponentId COMPONENT_ID =
      ComponentId.generateLazy("periodic_metric_reader");

  private final MetricExporter exporter;
  private final long intervalNanos;
  private final long exporterTimeoutNanos;
  private final int maxExportBatchSize;
  private final InternalTelemetryVersion internalTelemetryVersion;

  // Fires periodic tick signals. Does not run the actual export.
  private final ScheduledExecutorService scheduler;
  // Owns the export loop. Reads Signals from the queue and processes them sequentially.
  private final Thread worker;
  private final BlockingQueue<Signal> signals = new LinkedBlockingQueue<>();

  // True while a periodic tick is queued or being processed. Guards against multiple ticks
  // stacking up if the exporter is slower than the interval; matches the pre-refactor behavior of
  // dropping ticks while an export is in flight (no catch-up).
  private final AtomicBoolean tickPending = new AtomicBoolean(false);

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final Object startLock = new Object();

  private volatile CollectionRegistration collectionRegistration = CollectionRegistration.noop();
  @Nullable private volatile ScheduledFuture<?> scheduledFuture;

  private volatile MetricReaderInstrumentation instrumentation =
      new MetricReaderInstrumentation(COMPONENT_ID, MeterProvider.noop());

  /**
   * Returns a new {@link PeriodicMetricReader} which exports to the {@code exporter} once every
   * minute.
   */
  public static PeriodicMetricReader create(MetricExporter exporter) {
    return builder(exporter).build();
  }

  /** Returns a new {@link PeriodicMetricReaderBuilder}. */
  public static PeriodicMetricReaderBuilder builder(MetricExporter exporter) {
    return new PeriodicMetricReaderBuilder(exporter);
  }

  PeriodicMetricReader(
      MetricExporter exporter,
      long intervalNanos,
      long exporterTimeoutNanos,
      ScheduledExecutorService scheduler,
      int maxExportBatchSize,
      InternalTelemetryVersion internalTelemetryVersion) {
    this.exporter = exporter;
    this.intervalNanos = intervalNanos;
    this.exporterTimeoutNanos = exporterTimeoutNanos;
    this.scheduler = scheduler;
    this.maxExportBatchSize = maxExportBatchSize;
    this.internalTelemetryVersion = internalTelemetryVersion;
    this.worker =
        new DaemonThreadFactory("PeriodicMetricReader-worker").newThread(this::workerLoop);
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return exporter.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return exporter.getDefaultAggregation(instrumentType);
  }

  @Override
  public MemoryMode getMemoryMode() {
    return exporter.getMemoryMode();
  }

  /**
   * Forces a flush of all metrics.
   *
   * <p>If an export is already in progress, the flush will be queued behind it and completed when
   * the requested cycle finishes.
   */
  @Override
  public CompletableResultCode forceFlush() {
    if (shutdown.get()) {
      return CompletableResultCode.ofSuccess();
    }
    CompletableResultCode collectExport = new CompletableResultCode();
    signals.offer(new Signal(collectExport, /* poison= */ false));
    CompletableResultCode result = new CompletableResultCode();
    collectExport.whenComplete(
        () -> {
          CompletableResultCode flushResult = exporter.flush();
          flushResult.whenComplete(
              () -> {
                if (collectExport.isSuccess() && flushResult.isSuccess()) {
                  result.succeed();
                } else {
                  result.fail();
                }
              });
        });
    return result;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!shutdown.compareAndSet(false, true)) {
      return CompletableResultCode.ofSuccess();
    }

    ScheduledFuture<?> future = this.scheduledFuture;
    if (future != null) {
      future.cancel(false);
    }
    scheduler.shutdown();

    // Final flush + poison. Worker drains the flush signal, completes it, then exits on POISON.
    CompletableResultCode finalFlush = new CompletableResultCode();
    signals.offer(new Signal(finalFlush, /* poison= */ false));
    signals.offer(Signal.POISON);

    // Block until the worker drains the final flush and terminates. This preserves the
    // pre-refactor semantic that shutdown() does not return until the final export has completed.
    finalFlush.join(5, TimeUnit.SECONDS);
    try {
      worker.join(TimeUnit.SECONDS.toMillis(5));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    try {
      scheduler.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }

    CompletableResultCode result = new CompletableResultCode();
    CompletableResultCode exporterShutdown = exporter.shutdown();
    exporterShutdown.whenComplete(
        () -> {
          if (finalFlush.isSuccess() && exporterShutdown.isSuccess()) {
            result.succeed();
          } else {
            result.fail();
          }
        });
    return result;
  }

  @Override
  public void register(CollectionRegistration collectionRegistration) {
    this.collectionRegistration = collectionRegistration;
    start();
  }

  /**
   * Sets the {@link MeterProvider} to export metrics about this {@link PeriodicMetricReader} to.
   * Automatically called by the meter provider the reader is registered to.
   */
  @SuppressWarnings("UnusedMethod")
  private void setMeterProvider(MeterProvider meterProvider) {
    if (internalTelemetryVersion != InternalTelemetryVersion.LEGACY) {
      instrumentation = new MetricReaderInstrumentation(COMPONENT_ID, meterProvider);
    }
  }

  @Override
  public String toString() {
    return "PeriodicMetricReader{"
        + "exporter="
        + exporter
        + ", intervalNanos="
        + intervalNanos
        + ", maxExportBatchSize="
        + maxExportBatchSize
        + '}';
  }

  private void start() {
    synchronized (startLock) {
      if (!started.compareAndSet(false, true)) {
        return;
      }
      worker.start();
      scheduledFuture =
          scheduler.scheduleAtFixedRate(
              this::offerTick, intervalNanos, intervalNanos, TimeUnit.NANOSECONDS);
    }
  }

  private void offerTick() {
    // Coalesce: only enqueue a tick if none is currently pending or being processed. Matches the
    // prior behavior of dropping ticks while an export is in flight.
    if (tickPending.compareAndSet(false, true)) {
      signals.offer(Signal.TICK);
    }
  }

  private void workerLoop() {
    while (true) {
      Signal signal;
      try {
        signal = signals.take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      if (signal.poison) {
        return;
      }
      try {
        boolean success = runCycle();
        if (signal.flushResult != null) {
          if (success) {
            signal.flushResult.succeed();
          } else {
            signal.flushResult.fail();
          }
        }
      } finally {
        if (signal.isTick) {
          tickPending.set(false);
        }
      }
    }
  }

  /** Collect + export one cycle. Returns true iff collection and all batches succeeded. */
  private boolean runCycle() {
    long startNanoTime = CLOCK.nanoTime();
    String error = null;
    Collection<MetricData> metricData;
    try {
      metricData = collectionRegistration.collectAllMetrics();
    } catch (Throwable t) {
      error = t.getClass().getName();
      logger.log(Level.WARNING, "Exception thrown by the metric collection", t);
      return false;
    } finally {
      long durationNanos = CLOCK.nanoTime() - startNanoTime;
      instrumentation.recordCollection(durationNanos / 1_000_000_000.0, error);
    }

    if (metricData.isEmpty()) {
      logger.log(Level.FINE, "No metric data to export - skipping export.");
      return true;
    }

    try {
      return doExport(metricData);
    } catch (Throwable t) {
      logger.log(Level.WARNING, "Exporter threw an Exception", t);
      return false;
    }
  }

  private boolean doExport(Collection<MetricData> metricData) {
    if (maxExportBatchSize == 0) {
      return exportOne(metricData);
    }
    boolean anyFailed = false;
    for (Collection<MetricData> batch :
        MetricExportBatcher.batchMetrics(metricData, maxExportBatchSize)) {
      if (!exportOne(batch)) {
        anyFailed = true;
      }
    }
    return !anyFailed;
  }

  private boolean exportOne(Collection<MetricData> batch) {
    CompletableResultCode result = exporter.export(batch);
    // Block until the exporter completes or the timeout elapses. Safe because the worker thread
    // is dedicated to exports; no scheduler thread is held while we wait.
    result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
    if (!result.isDone()) {
      logger.log(Level.WARNING, "Exporter timed out");
      return false;
    }
    if (!result.isSuccess()) {
      logger.log(Level.WARNING, "Exporter failed");
      return false;
    }
    return true;
  }

  /**
   * Worker signal types: periodic tick (from the scheduler), flush request (from forceFlush or
   * final flush on shutdown), and poison pill (from shutdown to terminate the worker).
   */
  private static final class Signal {
    static final Signal TICK = new Signal(null, /* poison= */ false, /* isTick= */ true);
    static final Signal POISON = new Signal(null, /* poison= */ true, /* isTick= */ false);

    @Nullable final CompletableResultCode flushResult;
    final boolean poison;
    final boolean isTick;

    private Signal(@Nullable CompletableResultCode flushResult, boolean poison, boolean isTick) {
      this.flushResult = flushResult;
      this.poison = poison;
      this.isTick = isTick;
    }

    Signal(@Nullable CompletableResultCode flushResult, boolean poison) {
      this(flushResult, poison, /* isTick= */ false);
    }
  }
}
