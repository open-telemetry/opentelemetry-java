/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@code InstrumentProcessor} represents an internal instance of an {@code Accumulator} for a
 * specific {code Instrument}. It records individual measurements (via the {@code Aggregator}). It
 * batches together {@code Aggregator}s for the similar sets of labels.
 *
 * <p>The only thread safe method in this class is {@link #getAggregator()}. An entire collection
 * cycle must be protected by a lock. A collection cycle is defined by multiple calls to {@link
 * #batch(Labels, Aggregator, boolean)} followed by one {@link #completeCollectionCycle()};
 */
final class InstrumentProcessor {
  private final InstrumentDescriptor descriptor;
  private final Aggregation aggregation;
  private final Resource resource;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final Clock clock;
  private final AggregatorFactory aggregatorFactory;
  private Map<Labels, Aggregator> aggregatorMap;
  private long startEpochNanos;
  private final boolean delta;

  /**
   * Create a InstrumentAccumulator that uses the "cumulative" Temporality and uses all labels for
   * aggregation. "Cumulative" means that all metrics that are generated will be considered for the
   * lifetime of the Instrument being aggregated.
   */
  static InstrumentProcessor getCumulativeAllLabels(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregation aggregation) {
    return new InstrumentProcessor(
        descriptor,
        aggregation,
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        meterProviderSharedState.getClock(),
        /* delta= */ false);
  }

  /**
   * Create a InstrumentAccumulator that uses the "delta" Temporality and uses all labels for
   * aggregation. "Delta" means that all metrics that are generated are only for the most recent
   * collection interval.
   */
  static InstrumentProcessor getDeltaAllLabels(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregation aggregation) {
    return new InstrumentProcessor(
        descriptor,
        aggregation,
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        meterProviderSharedState.getClock(),
        /* delta= */ true);
  }

  private InstrumentProcessor(
      InstrumentDescriptor descriptor,
      Aggregation aggregation,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Clock clock,
      boolean delta) {
    this.descriptor = descriptor;
    this.aggregation = aggregation;
    this.resource = resource;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.clock = clock;
    this.aggregatorFactory = aggregation.getAggregatorFactory(descriptor.getValueType());
    this.delta = delta;
    this.aggregatorMap = new HashMap<>();
    startEpochNanos = clock.now();
  }

  /**
   * Returns the {@link Aggregator} that should be used by the bindings, or observers.
   *
   * @return the {@link Aggregator} used to aggregate individual events.
   */
  Aggregator getAggregator() {
    return aggregatorFactory.getAggregator();
  }

  /**
   * Batches multiple entries together that are part of the same metric. It may remove labels from
   * the {@link Labels} and merge aggregations together.
   *
   * @param labelSet the {@link Labels} associated with this {@code Aggregator}.
   * @param aggregator the {@link Aggregator} used to aggregate individual events for the given
   *     {@code LabelSetSdk}.
   * @param mappedAggregator {@code true} if the {@code Aggregator} is still in used by a binding.
   *     If {@code false} the {@code InstrumentProcessor} can reuse the {@code Aggregator} instance.
   */
  void batch(Labels labelSet, Aggregator aggregator, boolean mappedAggregator) {
    if (!aggregator.hasRecordings()) {
      return;
    }
    Aggregator currentAggregator = aggregatorMap.get(labelSet);
    if (currentAggregator == null) {
      // This aggregator is not mapped, we can use this instance.
      if (!mappedAggregator) {
        aggregatorMap.put(labelSet, aggregator);
        return;
      }
      currentAggregator = aggregatorFactory.getAggregator();
      aggregatorMap.put(labelSet, currentAggregator);
    }
    aggregator.mergeToAndReset(currentAggregator);
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
  List<MetricData> completeCollectionCycle() {
    long epochNanos = clock.now();
    if (aggregatorMap.isEmpty()) {
      return Collections.emptyList();
    }

    MetricData metricData =
        aggregation.toMetricData(
            resource,
            instrumentationLibraryInfo,
            descriptor,
            aggregatorMap,
            startEpochNanos,
            epochNanos);

    if (delta) {
      startEpochNanos = epochNanos;
      aggregatorMap = new HashMap<>();
    }

    return metricData == null ? Collections.emptyList() : Collections.singletonList(metricData);
  }

  /**
   * Returns whether this batcher generate "delta" style metrics. The alternative is "cumulative".
   */
  boolean generatesDeltas() {
    return delta;
  }
}
