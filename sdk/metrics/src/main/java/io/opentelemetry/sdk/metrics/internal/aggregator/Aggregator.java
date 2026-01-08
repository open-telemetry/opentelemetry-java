/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * Aggregator represents the abstract class for all the available aggregations that can be computed
 * during the collection phase for all the instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public interface Aggregator<T extends PointData> {
  /** Returns the drop aggregator, an aggregator that drops measurements. */
  static Aggregator<?> drop() {
    return DropAggregator.INSTANCE;
  }

  /**
   * Returns a new {@link AggregatorHandle}. This MUST by used by the synchronous to aggregate
   * recorded measurements during the collection cycle.
   *
   * @return a new {@link AggregatorHandle}.
   */
  AggregatorHandle<T> createHandle();

  /**
   * Returns a new DELTA point by computing the difference between two cumulative points.
   *
   * <p>Aggregators MUST implement diff if it can be used with asynchronous instruments.
   *
   * @param previousCumulative the previously captured point.
   * @param currentCumulative the newly captured (cumulative) point.
   * @return The resulting delta point.
   */
  default T diff(T previousCumulative, T currentCumulative) {
    throw new UnsupportedOperationException("This aggregator does not support diff.");
  }

  /**
   * Resets one reusable point to be a DELTA point by computing the difference between two
   * cumulative points.
   *
   * <p>The delta between the two points is set on {@code previousCumulativeReusable}
   *
   * <p>Aggregators MUST implement diff if it can be used with asynchronous instruments.
   *
   * @param previousCumulativeReusable the previously captured point.
   * @param currentCumulative the newly captured (cumulative) point.
   */
  default void diffInPlace(T previousCumulativeReusable, T currentCumulative) {
    throw new UnsupportedOperationException("This aggregator does not support diffInPlace.");
  }

  /** Creates a new reusable point. */
  default T createReusablePoint() {
    throw new UnsupportedOperationException(
        "This aggregator does not support createReusablePoint.");
  }

  /** Copies {@code point} into {@code toReusablePoint}. */
  default void copyPoint(T point, T toReusablePoint) {
    throw new UnsupportedOperationException("This aggregator does not support toPoint.");
  }

  /**
   * Returns the {@link MetricData} that this {@code Aggregation} will produce.
   *
   * @param resource the resource producing the metric.
   * @param instrumentationScopeInfo the scope that instrumented the metric.
   * @param metricDescriptor the name, description and unit of the metric.
   * @param points list of points
   * @param temporality the temporality of the metric.
   * @return the {@link MetricDataType} that this {@code Aggregation} will produce.
   */
  MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Collection<T> points,
      AggregationTemporality temporality);
}
