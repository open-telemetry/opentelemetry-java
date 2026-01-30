/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
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
  private final ScheduledExecutorService scheduler;
  private final Scheduled scheduled;
  private final Object lock = new Object();

  private volatile CollectionRegistration collectionRegistration = CollectionRegistration.noop();

  @Nullable private volatile ScheduledFuture<?> scheduledFuture;

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
      MetricExporter exporter, long intervalNanos, ScheduledExecutorService scheduler) {
    this.exporter = exporter;
    this.intervalNanos = intervalNanos;
    this.scheduler = scheduler;
    this.scheduled = new Scheduled();
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

  @Override
  public CompletableResultCode forceFlush() {
    CompletableResultCode result = new CompletableResultCode();
    CompletableResultCode doRunResult = scheduled.doRun();
    doRunResult.whenComplete(
        () -> {
          CompletableResultCode flushResult = exporter.flush();
          flushResult.whenComplete(
              () -> {
                if (doRunResult.isSuccess() && flushResult.isSuccess()) {
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
    CompletableResultCode result = new CompletableResultCode();
    ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
    if (scheduledFuture != null) {
      scheduledFuture.cancel(false);
    }
    scheduler.shutdown();
    try {
      scheduler.awaitTermination(5, TimeUnit.SECONDS);
      CompletableResultCode flushResult = scheduled.doRun();
      flushResult.join(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // force a shutdown if the export hasn't finished.
      scheduler.shutdownNow();
      // reset the interrupted status
      Thread.currentThread().interrupt();
    } finally {
      CompletableResultCode shutdownResult = scheduled.shutdown();
      shutdownResult.whenComplete(
          () -> {
            if (!shutdownResult.isSuccess()) {
              result.fail();
            } else {
              result.succeed();
            }
          });
    }
    return result;
  }

  @Override
  public void register(CollectionRegistration collectionRegistration) {
    this.collectionRegistration = collectionRegistration;
    start();
  }

  /**
   * Sets the {@link MeterProvider} to export metrics about this {@link PeriodicMetricReader} to.
   * Automatically called by the meter provide the reader is registered to.
   */
  void setMeterProvider(MeterProvider meterProvider) {
    this.scheduled.setMeterProvider(meterProvider);
  }

  @Override
  public String toString() {
    return "PeriodicMetricReader{"
        + "exporter="
        + exporter
        + ", intervalNanos="
        + intervalNanos
        + '}';
  }

  void start() {
    synchronized (lock) {
      if (scheduledFuture != null) {
        return;
      }
      scheduledFuture =
          scheduler.scheduleAtFixedRate(
              scheduled, intervalNanos, intervalNanos, TimeUnit.NANOSECONDS);
    }
  }

  private final class Scheduled implements Runnable {

    private final AtomicBoolean exportAvailable = new AtomicBoolean(true);

    private MetricReaderInstrumentation instrumentation =
        new MetricReaderInstrumentation(COMPONENT_ID, MeterProvider.noop());

    private Scheduled() {}

    void setMeterProvider(MeterProvider meterProvider) {
      instrumentation = new MetricReaderInstrumentation(COMPONENT_ID, meterProvider);
    }

    @Override
    public void run() {
      // Ignore the CompletableResultCode from doRun() in order to keep run() asynchronous
      doRun();
    }

    // Runs a collect + export cycle.
    CompletableResultCode doRun() {
      CompletableResultCode flushResult = new CompletableResultCode();
      if (exportAvailable.compareAndSet(true, false)) {
        try {
          long startNanoTime = CLOCK.nanoTime();
          String error = null;
          Collection<MetricData> metricData;
          try {
            metricData = collectionRegistration.collectAllMetrics();
          } catch (Throwable t) {
            error = t.getClass().getName();
            throw t;
          } finally {
            long durationNanos = CLOCK.nanoTime() - startNanoTime;
            instrumentation.recordCollection(durationNanos / 1_000_000_000.0, error);
          }
          if (metricData.isEmpty()) {
            logger.log(Level.FINE, "No metric data to export - skipping export.");
            flushResult.succeed();
            exportAvailable.set(true);
          } else {
            CompletableResultCode result = exporter.export(metricData);
            result.whenComplete(
                () -> {
                  if (!result.isSuccess()) {
                    logger.log(Level.FINE, "Exporter failed");
                  }

                  flushResult.succeed();
                  exportAvailable.set(true);
                });
          }
        } catch (Throwable t) {
          exportAvailable.set(true);
          logger.log(Level.WARNING, "Exporter threw an Exception", t);
          flushResult.fail();
        }
      } else {
        logger.log(Level.FINE, "Exporter busy. Dropping metrics.");
        flushResult.fail();
      }
      return flushResult;
    }

    CompletableResultCode shutdown() {
      return exporter.shutdown();
    }
  }
}
