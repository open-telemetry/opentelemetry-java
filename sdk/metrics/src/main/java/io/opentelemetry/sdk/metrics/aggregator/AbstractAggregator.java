/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.Measurement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract implementation of metrics aggregator that automatically bundles metrics streams into
 * a map by attributes.
 *
 * <p>Subclasses are expected to only implement the stateless methods:
 *
 * <ul>
 *   <li>{@link Aggregator#createStreamStorage} - Create a concurrent-friendly accumulator for
 *       sycnhronous instruments.
 *   <li>{@link #asyncAccumulation} - called for asynchronous instruments
 *   <li>{@link #merge} - Called when joining previous accumulation with current.
 *   <li>{@link #buildMetric} - When passed all accumulations, build the metric for this aggregator.
 * </ul>
 */
public abstract class AbstractAggregator<T> implements Aggregator<T> {
  private final long startEpochNanos;
  private long lastEpochNanos;
  private Map<Attributes, T> accumulationMap;

  AbstractAggregator(long startEpochNanos) {
    this.startEpochNanos = startEpochNanos;
    this.lastEpochNanos = startEpochNanos;
    this.accumulationMap = new HashMap<>();
  }

  @Override
  public void batchRecord(Measurement measurement) {
    batchStreamAccumulation(measurement.getAttributes(), asyncAccumulation(measurement));
  }

  @Override
  public final void batchStreamAccumulation(Attributes attributes, T accumulation) {
    T currentAccumulation = accumulationMap.get(attributes);
    if (currentAccumulation == null) {
      accumulationMap.put(attributes, accumulation);
      return;
    }
    accumulationMap.put(attributes, merge(currentAccumulation, accumulation));
  }

  @Override
  public final List<MetricData> completeCollectionCycle(long epochNanos) {
    if (accumulationMap.isEmpty()) {
      return Collections.emptyList();
    }
    MetricData metricData =
        buildMetric(accumulationMap, startEpochNanos, lastEpochNanos, epochNanos);
    lastEpochNanos = epochNanos;
    if (!isStatefulCollector()) {
      accumulationMap = new HashMap<>();
    }
    return metricData == null ? Collections.emptyList() : Collections.singletonList(metricData);
  }

  /**
   * Returns true if accumulated metrics stored by the ABstractAggregator should be preserved after
   * completing collection cycle.
   */
  protected abstract boolean isStatefulCollector();

  /** Merge the currently recording metric point with the previous accumulation. */
  abstract T merge(T current, T accumulated);

  /** Return the accumulation for an asynchronously recorded measurement. */
  abstract T asyncAccumulation(Measurement measurement);

  /**
   * Construct a metric stream.
   *
   * @param accumulated The underlying stream points.
   * @param startEpochNanos The start time for the metrics SDK.
   * @param lastEpochNanos The time of the last collection period (i.e. delta start time).
   * @param epochNanos The current collection period time (i.e. end time).
   */
  protected abstract MetricData buildMetric(
      Map<Attributes, T> accumulated, long startEpochNanos, long lastEpochNanos, long epochNanos);
}
