/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
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
   * @param resource The resource to attach these metrics against.
   * @param instrumentationScopeInfo The instrumentation scope that generated these metrics.
   * @param temporality The aggregation temporality requested by the reader.
   * @param currentAccumulation THe current accumulation of metric data from instruments. This might
   *     be delta (for synchronous) or cumulative (for asynchronous).
   * @param startEpochNanos The timestamp when the metrics SDK started.
   * @param epochNanos The current collection timestamp.
   * @return The {@link MetricData} points.
   */
  synchronized MetricData buildMetricFor(
      CollectionHandle collector,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      // Temporality is requested by the collector.
      AggregationTemporality temporality,
      Map<Attributes, T> currentAccumulation,
      long startEpochNanos,
      long epochNanos) {
    // In case it's our first collection, default to start timestamp.
    long lastCollectionEpoch = startEpochNanos;
    Map<Attributes, T> result = currentAccumulation;
    // Check our last report time.
    if (reportHistory.containsKey(collector)) {
      LastReportedAccumulation<T> last = reportHistory.get(collector);
      lastCollectionEpoch = last.getEpochNanos();
      // Use aggregation temporality + instrument to determine if we do a merge or a diff of
      // previous.  We have the following four scenarios:
      // 1. Delta Aggregation (temporality) + Cumulative recording (async instrument).
      //    Here we diff with last cumulative to get a delta.
      // 2. Cumulative Aggregation + Delta recording (sync instrument).
      //    Here we merge with our last record to get a cumulative aggregation.
      // 3. Cumulative Aggregation + Cumulative recording - do nothing
      // 4. Delta Aggregation + Delta recording - do nothing.
      if (temporality == AggregationTemporality.DELTA && !isSynchronous) {
        MetricStorageUtils.diffInPlace(last.getAccumulation(), currentAccumulation, aggregator);
        result = last.getAccumulation();
      } else if (temporality == AggregationTemporality.CUMULATIVE && isSynchronous) {
        // We need to make sure the current delta recording gets merged into the previous cumulative
        // for the next cumulative measurement.
        MetricStorageUtils.mergeAndPreserveInPlace(
            last.getAccumulation(), currentAccumulation, aggregator);
        // Note: We allow going over our hard limit on attribute streams when first merging, but
        // preserve after this point.
        if (last.getAccumulation().size() > MetricStorageUtils.MAX_ACCUMULATIONS) {
          MetricStorageUtils.removeUnseen(last.getAccumulation(), currentAccumulation);
        }
        result = last.getAccumulation();
      }
    }
    // Update last reported (cumulative) accumulation.
    // For synchronous instruments, we need the merge result.
    // For asynchronous instruments, we need the recorded value.
    // This assumes aggregation remains consistent for the lifetime of a collector, and
    // could be optimised to not record results for cases 3+4 listed above.
    if (isSynchronous) {
      // Sync instruments remember the full recording.
      reportHistory.put(collector, new LastReportedAccumulation<>(result, epochNanos));
    } else {
      // Async instruments record the raw measurement.
      reportHistory.put(collector, new LastReportedAccumulation<>(currentAccumulation, epochNanos));
    }
    if (result.isEmpty()) {
      return EmptyMetricData.getInstance();
    }
    return aggregator.toMetricData(
        resource,
        instrumentationScopeInfo,
        descriptor,
        result,
        temporality,
        startEpochNanos,
        lastCollectionEpoch,
        epochNanos);
  }

  /** Remembers what was presented to a specific exporter. */
  private static class LastReportedAccumulation<T> {
    private final Map<Attributes, T> accumulation;
    private final long epochNanos;

    /**
     * Constructs a new reporting record.
     *
     * @param accumulation The last accumulation of metric data.
     * @param epochNanos The timestamp the data was reported.
     */
    LastReportedAccumulation(Map<Attributes, T> accumulation, long epochNanos) {
      this.accumulation = accumulation;
      this.epochNanos = epochNanos;
    }

    long getEpochNanos() {
      return epochNanos;
    }

    Map<Attributes, T> getAccumulation() {
      return accumulation;
    }
  }
}
