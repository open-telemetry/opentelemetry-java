/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

abstract class AbstractSumAggregator<T> extends AbstractAggregator<T> {
  private final boolean isMonotonic;
  private final AggregationTemporality temporality;
  private final MergeStrategy mergeStrategy;

  AbstractSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      AggregationTemporality temporality) {
    super(
        resource,
        instrumentationLibraryInfo,
        metricDescriptor,
        resolveStateful(instrumentDescriptor.getType(), temporality));
    InstrumentType type = instrumentDescriptor.getType();
    this.isMonotonic = type == InstrumentType.COUNTER || type == InstrumentType.OBSERVABLE_SUM;
    this.temporality = temporality;
    this.mergeStrategy = resolveMergeStrategy(type, temporality);
  }

  /**
   * Resolve whether the aggregator should be stateful. For the special case {@link
   * InstrumentType#OBSERVABLE_SUM} and {@link InstrumentType#OBSERVABLE_UP_DOWN_SUM} instruments,
   * state is required if temporality is {@link AggregationTemporality#DELTA}. Because the observed
   * values are cumulative sums, we must maintain state to compute delta sums between collections.
   * For other instruments, state is required if temporality is {@link
   * AggregationTemporality#CUMULATIVE}.
   *
   * @param instrumentType the instrument type
   * @param temporality the temporality
   * @return whether the aggregator is stateful
   */
  private static boolean resolveStateful(
      InstrumentType instrumentType, AggregationTemporality temporality) {
    if (instrumentType == InstrumentType.OBSERVABLE_SUM
        || instrumentType == InstrumentType.OBSERVABLE_UP_DOWN_SUM) {
      return temporality == AggregationTemporality.DELTA;
    } else {
      return temporality == AggregationTemporality.CUMULATIVE;
    }
  }

  /**
   * Resolve the aggregator merge strategy. The merge strategy is SUM in all cases except where
   * temporality is {@link AggregationTemporality#DELTA} and instrument type is {@link
   * InstrumentType#OBSERVABLE_SUM} or {@link InstrumentType#OBSERVABLE_UP_DOWN_SUM}. In these
   * special cases, the observed values are cumulative sums so we must take a diff to compute the
   * delta sum.
   *
   * @param instrumentType the instrument type
   * @param temporality the temporality
   * @return the merge strategy
   */
  // Visible for testing
  static MergeStrategy resolveMergeStrategy(
      InstrumentType instrumentType, AggregationTemporality temporality) {
    if ((instrumentType == InstrumentType.OBSERVABLE_SUM
            || instrumentType == InstrumentType.OBSERVABLE_UP_DOWN_SUM)
        && temporality == AggregationTemporality.DELTA) {
      return MergeStrategy.DIFF;
    } else {
      return MergeStrategy.SUM;
    }
  }

  @Override
  public final T merge(T previousAccumulation, T accumulation) {
    switch (mergeStrategy) {
      case SUM:
        return mergeSum(previousAccumulation, accumulation);
      case DIFF:
        return mergeDiff(previousAccumulation, accumulation);
    }
    throw new IllegalStateException("Unsupported merge strategy: " + mergeStrategy.name());
  }

  abstract T mergeSum(T previousAccumulation, T accumulation);

  abstract T mergeDiff(T previousAccumulation, T accumulation);

  final boolean isMonotonic() {
    return isMonotonic;
  }

  final AggregationTemporality temporality() {
    return temporality;
  }

  enum MergeStrategy {
    SUM,
    DIFF
  }
}
