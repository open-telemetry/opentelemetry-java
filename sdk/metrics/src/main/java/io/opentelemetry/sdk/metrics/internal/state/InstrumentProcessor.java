/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * An {@code InstrumentProcessor} represents an internal instance of an {@code Accumulator} for a
 * specific {code Instrument}. It records individual measurements (via the {@code Aggregator}). It
 * batches together {@code Aggregator}s for the similar sets of attributes.
 *
 * <p>An entire collection cycle must be protected by a lock. A collection cycle is defined by
 * multiple calls to {@code #batch(...)} followed by one {@code #completeCollectionCycle(...)};
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
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
   * Batches multiple entries together that are part of the same metric. It may remove attributes
   * from the {@link Attributes} and merge aggregations together.
   *
   * @param attributes the {@link Attributes} associated with this {@code Aggregator}.
   * @param accumulation the accumulation produced by this instrument.
   */
  void batch(Attributes attributes, T accumulation) {
    T currentAccumulation = accumulationMap.putIfAbsent(attributes, accumulation);
    if (currentAccumulation != null) {
      accumulationMap.put(attributes, aggregator.merge(currentAccumulation, accumulation));
    }
  }

  /**
   * Ends the current collection cycle and returns the list of metrics batched in this Batcher.
   *
   * <p>There may be more than one MetricData in case a multi aggregator is configured.
   *
   * <p>Based on the configured options this method may reset the internal state to produce deltas,
   * or keep the internal state to produce cumulative metrics.
   *
   * @return the metric batched or {@code null}.
   */
  @Nullable
  MetricData completeCollectionCycle(long epochNanos) {
    if (accumulationMap.isEmpty()) {
      return null;
    }

    MetricData metricData =
        aggregator.toMetricData(accumulationMap, startEpochNanos, lastEpochNanos, epochNanos);

    lastEpochNanos = epochNanos;
    if (!aggregator.isStateful()) {
      accumulationMap = new HashMap<>();
    }

    return metricData;
  }
}
