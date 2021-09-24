/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Stores last reported time and (optional) accumulation for metrics. */
@ThreadSafe
class TemporalMetricStorage<T> {
  private final Aggregator<T> aggregator;
  private final boolean isSynchronous;
  private final Map<CollectionHandle, LastReportedAccumulation<T>> reportHistory = new HashMap<>();

  TemporalMetricStorage(Aggregator<T> aggregator, boolean isSynchronous) {
    this.aggregator = aggregator;
    this.isSynchronous = isSynchronous;
  }

  /**
   * Builds the {@link MetricData} streams to report against a specific metric reader.
   *
   * @param collector The handle of the metric reader.
   * @param currentAccumulation THe current accumulation of metric data from instruments. This might
   *     be delta (for synchronous) or cumulative (for asynchronous).
   * @param startEpochNanos The timestamp when the metrics SDK started.
   * @param epochNanos The current collection timestamp.
   * @return The {@link MetricData} points or {@code null}.
   */
  @Nullable
  synchronized MetricData buildMetricFor(
      CollectionHandle collector,
      Map<Attributes, T> currentAccumulation,
      long startEpochNanos,
      long epochNanos) {
    // In case it's our first collection, default to start timestmap.
    long lastCollectionEpoch = startEpochNanos;
    Map<Attributes, T> result = currentAccumulation;
    // Check our last report time.
    if (reportHistory.containsKey(collector)) {
      LastReportedAccumulation<T> last = reportHistory.get(collector);
      lastCollectionEpoch = last.getEpochNanos();
      // We need to merge with previous accumulation.
      if (aggregator.isStateful()) {
        // We merge the current into last, and take over that memory.
        DeltaMetricStorage.mergeInPlace(last.getAccumlation(), currentAccumulation, aggregator);
        result = last.getAccumlation();
      }
    }
    // Update last reported accumulation
    if (isSynchronous) {
      // Sync instruments remember the full recording.
      reportHistory.put(collector, new LastReportedAccumulation<>(result, epochNanos));
    } else {
      // Async instruments record the raw measurement.
      reportHistory.put(collector, new LastReportedAccumulation<>(currentAccumulation, epochNanos));
    }
    if (result.isEmpty()) {
      return null;
    }
    return aggregator.toMetricData(result, startEpochNanos, lastCollectionEpoch, epochNanos);
  }

  /** Remembers what was presented to a specific exporter. */
  private static class LastReportedAccumulation<T> {
    @Nullable private final Map<Attributes, T> accumulation;
    private final long epochNanos;

    /**
     * Constructs a new reporting record.
     *
     * @param accumulation The last accumulation of metric data or {@code null} if the accumulator
     *     is not stateful.
     * @param epochNanos The timestamp the data was reported.
     */
    LastReportedAccumulation(@Nullable Map<Attributes, T> accumulation, long epochNanos) {
      this.accumulation = accumulation;
      this.epochNanos = epochNanos;
    }

    long getEpochNanos() {
      return epochNanos;
    }

    @Nullable
    Map<Attributes, T> getAccumlation() {
      return accumulation;
    }
  }
}
