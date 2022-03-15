/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.export.AbstractMetricReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Wraps a {@link MetricExporter} and automatically reads and exports the metrics every export
 * interval. Metrics may also be dropped when it becomes time to export again, and there is an
 * export in progress.
 */
public final class PeriodicMetricReader extends AbstractMetricReader {
  private static final Logger logger = Logger.getLogger(PeriodicMetricReader.class.getName());

  private final MetricExporter exporter;
  private final ScheduledExecutorService scheduler;
  private final Scheduled scheduled;
  private final Object lock = new Object();

  @Nullable private volatile ScheduledFuture<?> scheduledFuture;

  /**
   * Returns a new {@link PeriodicMetricReader} which can be registered to a {@link
   * io.opentelemetry.sdk.metrics.SdkMeterProvider} to start a {@link PeriodicMetricReader}
   * exporting once every minute on a new daemon thread.
   */
  public static PeriodicMetricReader create(MetricExporter exporter) {
    return builder(exporter).build();
  }

  /** Returns a new {@link PeriodicMetricReaderBuilder}. */
  public static PeriodicMetricReaderBuilder builder(MetricExporter exporter) {
    return new PeriodicMetricReaderBuilder(exporter);
  }

  PeriodicMetricReader(MetricExporter exporter, ScheduledExecutorService scheduler) {
    this.exporter = exporter;
    this.scheduler = scheduler;
    this.scheduled = new Scheduled();
  }

  @Override
  @Nullable
  public AggregationTemporality getPreferredTemporality() {
    return exporter.getPreferredTemporality();
  }

  @Override
  public CompletableResultCode flush() {
    return scheduled.doRun();
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

  void start(long intervalNanos) {
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

    private Scheduled() {}

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
          CompletableResultCode result = exporter.export(getMetricProducer().collectAllMetrics());
          result.whenComplete(
              () -> {
                if (!result.isSuccess()) {
                  logger.log(Level.FINE, "Exporter failed");
                }
                flushResult.succeed();
                exportAvailable.set(true);
              });
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
