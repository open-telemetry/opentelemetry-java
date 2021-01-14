/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
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
  private Map<Labels, T> accumulationMap;
  private long startEpochNanos;
  private final boolean delta;

  /**
   * Create a new {@link InstrumentProcessor} for use in metric recording aggregation.
   *
   * <p>"Delta" temporality means that all metrics that are generated are only for the most recent
   * collection interval.
   *
   * <p>"Cumulative" temporality means that all metrics that are generated will be considered for
   * the lifetime of the Instrument being aggregated.
   */
  static <T> InstrumentProcessor<T> createProcessor(
      Aggregator<T> aggregator, long startEpochNanos, AggregationTemporality temporality) {
    return new InstrumentProcessor<>(aggregator, startEpochNanos, isDelta(temporality));
  }

  private InstrumentProcessor(Aggregator<T> aggregator, long startEpochNanos, boolean delta) {
    this.aggregator = aggregator;
    this.delta = delta;
    this.accumulationMap = new HashMap<>();
    this.startEpochNanos = startEpochNanos;
  }

  /**
   * Batches multiple entries together that are part of the same metric. It may remove labels from
   * the {@link Labels} and merge aggregations together.
   *
   * @param labelSet the {@link Labels} associated with this {@code Aggregator}.
   * @param accumulation the accumulation produced by this instrument.
   */
  void batch(Labels labelSet, T accumulation) {
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

    MetricData metricData = aggregator.toMetricData(accumulationMap, startEpochNanos, epochNanos);

    if (delta) {
      startEpochNanos = epochNanos;
      accumulationMap = new HashMap<>();
    }

    return metricData == null ? Collections.emptyList() : Collections.singletonList(metricData);
  }

  private static boolean isDelta(AggregationTemporality temporality) {
    switch (temporality) {
      case CUMULATIVE:
        return false;
      case DELTA:
        return true;
    }
    throw new IllegalStateException("unsupported Temporality: " + temporality);
  }
}
