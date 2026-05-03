/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.internal.DaemonThreadFactory;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Builder for {@link PeriodicMetricReader}.
 *
 * @since 1.14.0
 */
public final class PeriodicMetricReaderBuilder {

  static final long DEFAULT_SCHEDULE_DELAY_MINUTES = 1;

  private final MetricExporter metricExporter;

  private long intervalNanos = TimeUnit.MINUTES.toNanos(DEFAULT_SCHEDULE_DELAY_MINUTES);

  @Nullable private ScheduledExecutorService executor;

  private int maxExportBatchSize;

  PeriodicMetricReaderBuilder(MetricExporter metricExporter) {
    this.metricExporter = metricExporter;
  }

  /**
   * Sets the interval of reads. If unset, defaults to {@value DEFAULT_SCHEDULE_DELAY_MINUTES}min.
   */
  public PeriodicMetricReaderBuilder setInterval(long interval, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(interval > 0, "interval must be positive");
    intervalNanos = unit.toNanos(interval);
    return this;
  }

  /**
   * Sets the interval of reads. If unset, defaults to {@value DEFAULT_SCHEDULE_DELAY_MINUTES}min.
   */
  public PeriodicMetricReaderBuilder setInterval(Duration interval) {
    requireNonNull(interval, "interval");
    return setInterval(interval.toNanos(), TimeUnit.NANOSECONDS);
  }

  /** Sets the {@link ScheduledExecutorService} to schedule reads on. */
  public PeriodicMetricReaderBuilder setExecutor(ScheduledExecutorService executor) {
    requireNonNull(executor, "executor");
    this.executor = executor;
    return this;
  }

  /**
   * Sets the maximum number of data points to include in a single export batch. If unset, no
   * batching will be performed. The maximum number of data points is considered across MetricData
   * objects scheduled for export.
   *
   * @param maxExportBatchSize The maximum number of data points to include in a single export
   *     batch.
   */
  PeriodicMetricReaderBuilder setMaxExportBatchSize(int maxExportBatchSize) {
    checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive");
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /** Build a {@link PeriodicMetricReader} with the configuration of this builder. */
  public PeriodicMetricReader build() {
    ScheduledExecutorService executor = this.executor;
    if (executor == null) {
      executor =
          Executors.newScheduledThreadPool(1, new DaemonThreadFactory("PeriodicMetricReader"));
    }
    return new PeriodicMetricReader(metricExporter, intervalNanos, executor, maxExportBatchSize);
  }
}
