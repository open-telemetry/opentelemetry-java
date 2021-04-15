/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;

abstract class AbstractSumAggregator<T> extends AbstractAggregator<T> {
  private final boolean isMonotonic;
  private final AggregationTemporality temporality;
  private final MergeStrategy mergeStrategy;

  AbstractSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      AggregationTemporality temporality) {
    super(
        resource,
        instrumentationLibraryInfo,
        instrumentDescriptor,
        resolveStateful(instrumentDescriptor.getType(), temporality));
    InstrumentType type = instrumentDescriptor.getType();
    this.isMonotonic = type == InstrumentType.COUNTER || type == InstrumentType.SUM_OBSERVER;
    this.temporality = temporality;
    this.mergeStrategy = resolveMergeStrategy(type, temporality);
  }

  /**
   * Resolve whether the aggregator should be stateful. For the special case {@link
   * InstrumentType#SUM_OBSERVER} and {@link InstrumentType#UP_DOWN_SUM_OBSERVER} instruments, state
   * is required if temporality is {@link AggregationTemporality#DELTA}. Because the observed values
   * are cumulative sums, we must maintain state to compute delta sums between collections. For
   * other instruments, state is required if temporality is {@link
   * AggregationTemporality#CUMULATIVE}.
   *
   * @param instrumentType the instrument type
   * @param temporality the temporality
   * @return whether the aggregator is stateful
   */
  private static boolean resolveStateful(
      InstrumentType instrumentType, AggregationTemporality temporality) {
    if (instrumentType == InstrumentType.SUM_OBSERVER
        || instrumentType == InstrumentType.UP_DOWN_SUM_OBSERVER) {
      return temporality == AggregationTemporality.DELTA;
    } else {
      return temporality == AggregationTemporality.CUMULATIVE;
    }
  }

  /**
   * Resolve the aggregator merge strategy. The merge strategy is SUM in all cases except where
   * temporality is {@link AggregationTemporality#DELTA} and instrument type is {@link
   * InstrumentType#SUM_OBSERVER} or {@link InstrumentType#UP_DOWN_SUM_OBSERVER}. In these special
   * cases, the observed values are cumulative sums so we must take a diff to compute the delta sum.
   *
   * @param instrumentType the instrument type
   * @param temporality the temporality
   * @return the merge strategy
   */
  static MergeStrategy resolveMergeStrategy(
      InstrumentType instrumentType, AggregationTemporality temporality) {
    if ((instrumentType == InstrumentType.SUM_OBSERVER
            || instrumentType == InstrumentType.UP_DOWN_SUM_OBSERVER)
        && temporality == AggregationTemporality.DELTA) {
      return MergeStrategy.DIFF;
    } else {
      return MergeStrategy.SUM;
    }
  }

  @Override
  public final T merge(T a1, T a2) {
    switch (mergeStrategy) {
      case SUM:
        return mergeSum(a1, a2);
      case DIFF:
        return mergeDiff(a1, a2);
    }
    throw new AssertionError("Unsupported merge strategy: " + mergeStrategy.name());
  }

  abstract T mergeSum(T a1, T a2);

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
