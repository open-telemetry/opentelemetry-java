/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;

/**
 * A {@code Batcher} represents an internal representation of an {code Instrument} aggregation
 * process. It records individual measurements (via the {@code Aggregator}). It batches together
 * {@code Aggregator}s for the similar sets of labels.
 *
 * <p>The only thread safe method in this class is {@link #getAggregator()}. An entire collection
 * cycle must be protected by a lock. A collection cycle is defined by multiple calls to {@link
 * #batch(Labels, Aggregator, boolean)} followed by one {@link #completeCollectionCycle()};
 */
interface Batcher {

  /**
   * Returns the {@link Aggregator} that should be used by the bindings, or observers.
   *
   * @return the {@link Aggregator} used to aggregate individual events.
   */
  Aggregator getAggregator();

  /**
   * Batches multiple entries together that are part of the same metric. It may remove labels from
   * the {@link Labels} and merge aggregations together.
   *
   * @param labelSet the {@link Labels} associated with this {@code Aggregator}.
   * @param aggregator the {@link Aggregator} used to aggregate individual events for the given
   *     {@code LabelSetSdk}.
   * @param mappedAggregator {@code true} if the {@code Aggregator} is still in used by a binding.
   *     If {@code false} the {@code Batcher} can reuse the {@code Aggregator} instance.
   */
  void batch(Labels labelSet, Aggregator aggregator, boolean mappedAggregator);

  /**
   * Ends the current collection cycle and returns the list of metrics batched in this Batcher.
   *
   * <p>There may be more than one MetricData in case a multi aggregator is configured.
   *
   * <p>Based on the configured options this method may reset the internal state to produce deltas,
   * or keep the internal state to produce cumulative metrics.
   *
   * @return the list of metrics batched in this Batcher.
   */
  List<MetricData> completeCollectionCycle();
}
