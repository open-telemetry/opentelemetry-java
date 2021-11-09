/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Aggregator represents the abstract class for all the available aggregations that can be computed
 * during the accumulation phase for all the instrument.
 *
 * <p>The synchronous instruments will create an {@link AggregatorHandle} to record individual
 * measurements synchronously, and for asynchronous the {@link #accumulateDoubleMeasurement} or
 * {@link #accumulateLongMeasurement} will be used when reading values from the instrument
 * callbacks.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public interface Aggregator<T> {
  /**
   * Returns the empty aggregator, an aggregator that never records measurements or reports values.
   */
  static Aggregator<Void> empty() {
    return EmptyAggregator.INSTANCE;
  }
  /**
   * Returns a new {@link AggregatorHandle}. This MUST by used by the synchronous to aggregate
   * recorded measurements during the collection cycle.
   *
   * @return a new {@link AggregatorHandle}.
   */
  AggregatorHandle<T> createHandle();

  /**
   * Returns a new {@code Accumulation} for the given value. This MUST be used by the asynchronous
   * instruments to create {@code Accumulation} that are passed to the processor.
   *
   * @param value the given value to be used to create the {@code Accumulation}.
   * @return a new {@code Accumulation} for the given value, or {@code null} if there are no recordings.
   */
  @Nullable
  default T accumulateLongMeasurement(long value, Attributes attributes, Context context) {
    AggregatorHandle<T> handle = createHandle();
    handle.recordLong(value, attributes, context);
    return handle.accumulateThenReset(attributes);
  }

  /**
   * Returns a new {@code Accumulation} for the given value. This MUST be used by the asynchronous
   * instruments to create {@code Accumulation} that are passed to the processor.
   *
   * @param value the given value to be used to create the {@code Accumulation}.
   * @return a new {@code Accumulation} for the given value, or {@code null} if there are no recordings.
   */
  @Nullable
  default T accumulateDoubleMeasurement(double value, Attributes attributes, Context context) {
    AggregatorHandle<T> handle = createHandle();
    handle.recordDouble(value, attributes, context);
    return handle.accumulateThenReset(attributes);
  }

  /**
   * Returns the result of the merge of the given accumulations.
   *
   * <p>This should always assume that the accumulations do not overlap and merge together for a new
   * cumulative report.
   *
   * @param previousCumulative the previously captured accumulation
   * @param delta the newly captured (delta) accumulation
   * @return the result of the merge of the given accumulations.
   */
  T merge(T previousCumulative, T delta);

  /**
   * Returns a new DELTA aggregation by comparing two cumulative measurements.
   *
   * @param previousCumulative the previously captured accumulation.
   * @param currentCumulative the newly captured (cumulative) accumulation.
   * @return The resulting delta accumulation.
   */
  T diff(T previousCumulative, T currentCumulative);

  /**
   * Returns the {@link MetricData} that this {@code Aggregation} will produce.
   *
   * @param resource the resource producing the metric.
   * @param instrumentationLibrary the library that instrumented the metric.
   * @param metricDescriptor the name, description and unit of the metric.
   * @param accumulationByLabels the map of Labels to Accumulation.
   * @param temporality the temporality of the accumulation.
   * @param startEpochNanos the startEpochNanos for the {@code Point}.
   * @param epochNanos the epochNanos for the {@code Point}.
   * @return the {@link MetricDataType} that this {@code Aggregation} will produce.
   */
  @Nullable
  MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibrary,
      MetricDescriptor metricDescriptor,
      Map<Attributes, T> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos);
}
