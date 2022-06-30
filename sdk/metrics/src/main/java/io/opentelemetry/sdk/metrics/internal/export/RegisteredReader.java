/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/**
 * Represents a {@link MetricReader} registered with {@link SdkMeterProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class RegisteredReader {

  private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);
  private final int id = ID_COUNTER.incrementAndGet();
  private final MetricReader metricReader;
  private final ViewRegistry viewRegistry;
  private volatile long lastCollectEpochNanos;

  /** Construct a new collection info object storing information for collection against a reader. */
  public static RegisteredReader create(MetricReader reader, ViewRegistry viewRegistry) {
    return new RegisteredReader(reader, viewRegistry);
  }

  private RegisteredReader(MetricReader metricReader, ViewRegistry viewRegistry) {
    this.metricReader = metricReader;
    this.viewRegistry = viewRegistry;
  }

  public MetricReader getReader() {
    return metricReader;
  }

  /**
   * Set the time the last collection took place for the reader.
   *
   * <p>Called by {@link SdkMeterProvider}'s {@link MetricProducer} after collection.
   */
  public void setLastCollectEpochNanos(long epochNanos) {
    this.lastCollectEpochNanos = epochNanos;
  }

  /**
   * Get the time of the last collection for the reader.
   *
   * <p>Used to compute the {@link PointData#getStartEpochNanos()} for instruments aggregations with
   * {@link AggregationTemporality#DELTA} temporality.
   */
  public long getLastCollectEpochNanos() {
    return lastCollectEpochNanos;
  }

  /** Get the {@link ViewRegistry} for the reader. */
  public ViewRegistry getViewRegistry() {
    return viewRegistry;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegisteredReader)) {
      return false;
    }
    return id == ((RegisteredReader) o).id;
  }

  @Override
  public String toString() {
    return "RegisteredReader{" + id + "}";
  }
}
