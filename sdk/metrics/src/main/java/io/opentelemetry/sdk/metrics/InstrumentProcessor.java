/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@code InstrumentProcessor} represents an internal instance of an {@code Accumulator} for a
 * specific {code Instrument}. It records individual measurements (via the {@code Aggregator}). It
 * batches together {@code Aggregator}s for the similar sets of labels.
 *
 * <p>An entire collection cycle must be protected by a lock. A collection cycle is defined by
 * multiple calls to {@code #batch(...)} followed by one {@code #completeCollectionCycle(...)};
 */
final class InstrumentProcessor<T> {
  private final Aggregator<T> aggregator;
  private final long startEpochNanos;
  private long lastEpochNanos;
  private Map<Attributes, T> accumulationMap;

  InstrumentProcessor(Aggregator<T> aggregator, long startEpochNanos) {
    this.aggregator = aggregator;
    this.startEpochNanos = startEpochNanos;
    this.lastEpochNanos = startEpochNanos;
    this.accumulationMap = new HashMap<>();
  }

  /**
   * Batches multiple entries together that are part of the same metric. It may remove labels from
   * the {@link Labels} and merge aggregations together.
   *
   * @param labelSet the {@link Labels} associated with this {@code Aggregator}.
   * @param accumulation the accumulation produced by this instrument.
   */
  void batch(Attributes labelSet, T accumulation) {
    T currentAccumulation = accumulationMap.get(labelSet);
    if (currentAccumulation == null) {
      accumulationMap.put(labelSet, accumulation);
      return;
    }
    accumulationMap.put(labelSet, aggregator.merge(currentAccumulation, accumulation));
  }

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
  List<MetricData> completeCollectionCycle(long epochNanos) {
    if (accumulationMap.isEmpty()) {
      return Collections.emptyList();
    }

    MetricData metricData =
        aggregator.toMetricData(accumulationMap, startEpochNanos, lastEpochNanos, epochNanos);

    lastEpochNanos = epochNanos;
    if (!aggregator.isStateful()) {
      accumulationMap = new HashMap<>();
    }

    return metricData == null ? Collections.emptyList() : Collections.singletonList(metricData);
  }
}
