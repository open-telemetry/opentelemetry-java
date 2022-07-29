/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.SdkMeterProvider.MAX_ACCUMULATIONS;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/** Stores last reported time and (optional) accumulation for metrics. */
@ThreadSafe
class TemporalMetricStorage<T, U extends ExemplarData> {
  private final Aggregator<T, U> aggregator;
  private final boolean isSynchronous;
  private final RegisteredReader registeredReader;
  private Map<Attributes, T> lastAccumulation = new HashMap<>();
  private final AggregationTemporality temporality;
  private final MetricDescriptor metricDescriptor;

  TemporalMetricStorage(
      Aggregator<T, U> aggregator,
      boolean isSynchronous,
      RegisteredReader registeredReader,
      AggregationTemporality aggregationTemporality,
      MetricDescriptor metricDescriptor) {
    this.aggregator = aggregator;
    this.isSynchronous = isSynchronous;
    this.registeredReader = registeredReader;
    this.temporality = aggregationTemporality;
    this.metricDescriptor = metricDescriptor;
  }

  /**
   * Builds the {@link MetricData} for the {@code currentAccumulation}.
   *
   * @param resource The resource to attach these metrics against.
   * @param instrumentationScopeInfo The instrumentation scope that generated these metrics.
   * @param currentAccumulation The current accumulation of metric data from instruments. This might
   *     be delta (for synchronous) or cumulative (for asynchronous).
   * @param startEpochNanos The timestamp when the metrics SDK started.
   * @param epochNanos The current collection timestamp.
   * @return The {@link MetricData} points.
   */
  synchronized MetricData buildMetricFor(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      Map<Attributes, T> currentAccumulation,
      long startEpochNanos,
      long epochNanos) {
    return buildMetricFor(resource, instrumentationScopeInfo, currentAccumulation,
        startEpochNanos, epochNanos, MAX_ACCUMULATIONS);
  }

  /**
   * Builds the {@link MetricData} for the {@code currentAccumulation}.
   *
   * @param resource The resource to attach these metrics against.
   * @param instrumentationScopeInfo The instrumentation scope that generated these metrics.
   * @param currentAccumulation The current accumulation of metric data from instruments. This might
   *     be delta (for synchronous) or cumulative (for asynchronous).
   * @param startEpochNanos The timestamp when the metrics SDK started.
   * @param epochNanos The current collection timestamp.
   * @return The {@link MetricData} points.
   */
  synchronized MetricData buildMetricFor(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      Map<Attributes, T> currentAccumulation,
      long startEpochNanos,
      long epochNanos,
      int maxAccumulations) {

    Map<Attributes, T> result = currentAccumulation;
    long lastCollectionEpoch = registeredReader.getLastCollectEpochNanos();
    // Use aggregation temporality + instrument to determine if we do a merge or a diff of
    // previous.  We have the following four scenarios:
    // 1. Delta Aggregation (temporality) + Cumulative recording (async instrument).
    //    Here we diff with last cumulative to get a delta.
    // 2. Cumulative Aggregation + Delta recording (sync instrument).
    //    Here we merge with our last record to get a cumulative aggregation.
    // 3. Cumulative Aggregation + Cumulative recording - do nothing
    // 4. Delta Aggregation + Delta recording - do nothing.
    if (temporality == AggregationTemporality.DELTA && !isSynchronous) {
      MetricStorageUtils.diffInPlace(lastAccumulation, currentAccumulation, aggregator);
      result = lastAccumulation;
    } else if (temporality == AggregationTemporality.CUMULATIVE && isSynchronous) {
      // We need to make sure the current delta recording gets merged into the previous cumulative
      // for the next cumulative measurement.
      MetricStorageUtils.mergeAndPreserveInPlace(lastAccumulation, currentAccumulation, aggregator);
      // Note: We allow going over our hard limit on attribute streams when first merging, but
      // preserve after this point.
      if (lastAccumulation.size() > maxAccumulations) {
        MetricStorageUtils.removeUnseen(lastAccumulation, currentAccumulation);
      }
      result = lastAccumulation;
    }

    // Update last reported (cumulative) accumulation.
    // For synchronous instruments, we need the merge result.
    // For asynchronous instruments, we need the recorded value.
    // This assumes aggregation remains consistent for the lifetime of a collector, and
    // could be optimised to not record results for cases 3+4 listed above.
    if (isSynchronous) {
      // Sync instruments remember the full recording.
      lastAccumulation = result;
    } else {
      // Async instruments record the raw measurement.
      lastAccumulation = currentAccumulation;
    }
    if (result.isEmpty()) {
      return EmptyMetricData.getInstance();
    }
    return aggregator.toMetricData(
        resource,
        instrumentationScopeInfo,
        metricDescriptor,
        result,
        temporality,
        startEpochNanos,
        lastCollectionEpoch,
        epochNanos);
  }
}
