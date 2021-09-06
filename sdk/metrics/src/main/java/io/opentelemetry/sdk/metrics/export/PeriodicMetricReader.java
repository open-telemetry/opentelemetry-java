/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps a {@link MetricExporter} and automatically reads and exports the metrics every
 * export interval. Metrics may also be dropped when it becomes time to export again, and there is
 * an export in progress.
 */
public class PeriodicMetricReader implements MetricReader {
  private static final Logger logger = Logger.getLogger(PeriodicMetricReader.class.getName());

  private final MetricProducer producer;
  private final MetricExporter exporter;
  private final ScheduledExecutorService scheduler;

  private final Scheduled scheduled;
  private volatile ScheduledFuture<?> scheduledFuture;
  private final Object lock = new Object();

  private PeriodicMetricReader(
      MetricProducer producer, MetricExporter exporter, ScheduledExecutorService scheduler) {
    this.producer = producer;
    this.exporter = exporter;
    this.scheduler = scheduler;
    this.scheduled = new Scheduled();
  }

  @Override
  public CompletableResultCode flush() {
    return scheduled.doRun();
  }

  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = new CompletableResultCode();
    if (scheduledFuture != null) {
      scheduledFuture.cancel(false);
    }
    scheduler.shutdown();
    try {
      scheduler.awaitTermination(5, TimeUnit.SECONDS);
      final CompletableResultCode flushResult = scheduled.doRun();
      flushResult.join(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // force a shutdown if the export hasn't finished.
      scheduler.shutdownNow();
      // reset the interrupted status
      Thread.currentThread().interrupt();
    } finally {
      final CompletableResultCode shutdownResult = scheduled.shutdown();
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

  private void start(Duration duration) {
    synchronized (lock) {
      if (scheduledFuture != null) {
        return;
      }
      scheduledFuture =
          scheduler.scheduleAtFixedRate(
              scheduled, duration.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Factory for {@link PeriodicMetricReader}.
   */
  public static class Factory implements MetricReader.Factory<PeriodicMetricReader> {
    private final MetricExporter exporter;
    private final Duration duration;
    private final ScheduledExecutorService scheduler;

    /**
     * Builds a factory that will register and start a PeriodMetricReader.
     * 
     * <p> This will export once every 5 minutes.
     * 
     * <p> This will spin up a new daemon thread to schedule the export on.
     * 
     * @param duration The duration (interval) between metric export calls.
     */
    public Factory(MetricExporter exporter) {
      this(exporter, Duration.ofMinutes(5));
    }

    /**
     * Builds a factory that will register and start a PeriodMetricReader.
     * 
     * <p> This will spin up a new daemon thread to schedule the export on.
     * 
     * @param exporter The exporter receiving metrics.
     * @param duration The duration (interval) between metric export calls.
     */
    public Factory(MetricExporter exporter, Duration duration) {
      this(
          exporter,
          duration,
          Executors.newScheduledThreadPool(1, new DaemonThreadFactory("IntervalMetricReader")));
    }

    /**
     * Builds a factory that will register and start a PeriodMetricReader.
     * 
     * @param exporter The exporter receiving metrics.
     * @param duration The duration (interval) between metric export calls.
     * @param scheduler The service to schedule export work.
     */
    public Factory(MetricExporter exporter, Duration duration, ScheduledExecutorService scheduler) {
      this.exporter = exporter;
      this.duration = duration;
      this.scheduler = scheduler;
    }

    @Override
    public PeriodicMetricReader apply(MetricProducer producer) {
      PeriodicMetricReader result = new PeriodicMetricReader(producer, exporter, scheduler);
      result.start(duration);
      return result;
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
      final CompletableResultCode flushResult = new CompletableResultCode();
      if (exportAvailable.compareAndSet(true, false)) {
        try {
          final CompletableResultCode result = exporter.export(producer.collectAllMetrics());
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
