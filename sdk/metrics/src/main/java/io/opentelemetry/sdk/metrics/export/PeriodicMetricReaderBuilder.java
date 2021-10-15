/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** A builder for {@link PeriodicMetricReader}. */
public final class PeriodicMetricReaderBuilder {

  static final long DEFAULT_SCHEDULE_DELAY_MINUTES = 1;

  private final MetricExporter metricExporter;

  private long intervalNanos = TimeUnit.MINUTES.toNanos(DEFAULT_SCHEDULE_DELAY_MINUTES);

  @Nullable private ScheduledExecutorService executor;

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
   * Returns a new {@link MetricReaderFactory} with the configuration of this builder which can be
   * registered with a {@link io.opentelemetry.sdk.metrics.SdkMeterProvider}.
   */
  public MetricReaderFactory newMetricReaderFactory() {
    ScheduledExecutorService executor = this.executor;
    if (executor == null) {
      executor =
          Executors.newScheduledThreadPool(1, new DaemonThreadFactory("PeriodicMetricReader"));
    }
    return new PeriodicMetricReaderFactory(metricExporter, intervalNanos, executor);
  }
}
