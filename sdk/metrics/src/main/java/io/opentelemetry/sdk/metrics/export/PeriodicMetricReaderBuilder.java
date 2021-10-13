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

public final class PeriodicMetricReaderBuilder {

  static final long DEFAULT_SCHEDULE_DELAY_MINUTES = 5;

  private final MetricExporter metricExporter;

  private long scheduleDelayNanos = TimeUnit.MINUTES.toNanos(DEFAULT_SCHEDULE_DELAY_MINUTES);

  @Nullable private ScheduledExecutorService executor;

  PeriodicMetricReaderBuilder(MetricExporter metricExporter) {
    this.metricExporter = metricExporter;
  }

  /**
   * Sets the delay interval between two consecutive reads. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_MINUTES}min.
   */
  public PeriodicMetricReaderBuilder setScheduleDelay(long delay, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(delay >= 0, "delay must be non-negative");
    scheduleDelayNanos = unit.toNanos(delay);
    return this;
  }

  /**
   * Sets the delay interval between two consecutive reads. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_MINUTES}min.
   */
  public PeriodicMetricReaderBuilder setScheduleDelay(Duration delay) {
    requireNonNull(delay, "delay");
    return setScheduleDelay(delay.toNanos(), TimeUnit.NANOSECONDS);
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
    return new PeriodicMetricReaderFactory(metricExporter, scheduleDelayNanos, executor);
  }
}
