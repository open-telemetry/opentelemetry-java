/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** Factory for {@link PeriodicMetricReader}. */
public class PeriodicMetricReaderFactory implements MetricReaderFactory {
  private final MetricExporter exporter;
  private final Duration duration;
  private final ScheduledExecutorService scheduler;

  /**
   * Builds a factory that will register and start a PeriodMetricReader.
   *
   * <p>This will export once every 5 minutes.
   *
   * <p>This will spin up a new daemon thread to schedule the export on.
   *
   * @param exporter The exporter receiving metrics.
   */
  public PeriodicMetricReaderFactory(MetricExporter exporter) {
    this(exporter, Duration.ofMinutes(5));
  }

  /**
   * Builds a factory that will register and start a PeriodMetricReader.
   *
   * <p>This will spin up a new daemon thread to schedule the export on.
   *
   * @param exporter The exporter receiving metrics.
   * @param duration The duration (interval) between metric export calls.
   */
  public PeriodicMetricReaderFactory(MetricExporter exporter, Duration duration) {
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
  public PeriodicMetricReaderFactory(
      MetricExporter exporter, Duration duration, ScheduledExecutorService scheduler) {
    this.exporter = exporter;
    this.duration = duration;
    this.scheduler = scheduler;
  }

  @Override
  public MetricReader apply(MetricProducer producer) {
    PeriodicMetricReader result = new PeriodicMetricReader(producer, exporter, scheduler);
    // TODO - allow a different start delay.
    result.start(duration);
    return result;
  }
}
