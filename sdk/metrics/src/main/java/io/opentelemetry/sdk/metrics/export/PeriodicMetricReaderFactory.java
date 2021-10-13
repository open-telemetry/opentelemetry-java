/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import java.util.concurrent.ScheduledExecutorService;

final class PeriodicMetricReaderFactory implements MetricReaderFactory {
  private final MetricExporter exporter;
  private final long scheduleDelayNanos;
  private final ScheduledExecutorService scheduler;

  PeriodicMetricReaderFactory(
      MetricExporter exporter, long scheduleDelayNanos, ScheduledExecutorService scheduler) {
    this.exporter = exporter;
    this.scheduleDelayNanos = scheduleDelayNanos;
    this.scheduler = scheduler;
  }

  @Override
  public MetricReader apply(MetricProducer producer) {
    PeriodicMetricReader result = new PeriodicMetricReader(producer, exporter, scheduler);
    // TODO - allow a different start delay.
    result.start(scheduleDelayNanos);
    return result;
  }
}
