/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Aggregator represents the abstract class for all the available aggregations that can be computed
 * during the accumulation phase for all the instrument.
 *
 * <p>The synchronous instruments will create an {@link AggregatorHandle} to record individual
 * measurements synchronously, and for asynchronous the {@link #accumulateDouble(double)} or {@link
 * #accumulateLong(long)} will be used when reading values from the instrument callbacks.
 */
@Immutable
public interface Aggregator<T> {
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
   * @return a new {@code Accumulation} for the given value.
   */
  default T accumulateLong(long value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording long values.");
  }

  /**
   * Returns a new {@code Accumulation} for the given value. This MUST be used by the asynchronous
   * instruments to create {@code Accumulation} that are passed to the processor.
   *
   * @param value the given value to be used to create the {@code Accumulation}.
   * @return a new {@code Accumulation} for the given value.
   */
  default T accumulateDouble(double value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording double values.");
  }

  /**
   * Returns the result of the merge of the given accumulations.
   *
   * @param previousAccumulation the previously captured accumulation
   * @param accumulation the newly captured accumulation
   * @return the result of the merge of the given accumulations.
   */
  T merge(T previousAccumulation, T accumulation);

  /**
   * Returns {@code true} if the processor needs to keep the previous collected state in order to
   * compute the desired metric.
   *
   * @return {@code true} if the processor needs to keep the previous collected state.
   */
  boolean isStateful();

  /**
   * Returns the {@link MetricData} that this {@code Aggregation} will produce.
   *
   * @param accumulationByLabels the map of Labels to Accumulation.
   * @param startEpochNanos the startEpochNanos for the {@code Point}.
   * @param epochNanos the epochNanos for the {@code Point}.
   * @return the {@link MetricDataType} that this {@code Aggregation} will produce.
   */
  @Nullable
  MetricData toMetricData(
      Map<Attributes, T> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos);
}
