/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ImmutableAggregationFactory<L extends Accumulation, D extends Accumulation>
    implements AggregationFactory {
  static final AggregationFactory SUM =
      new ImmutableAggregationFactory<>(
          SumAggregation.LONG_INSTANCE, SumAggregation.DOUBLE_INSTANCE);

  static final AggregationFactory COUNT =
      new ImmutableAggregationFactory<>(CountAggregation.INSTANCE, CountAggregation.INSTANCE);

  static final AggregationFactory LAST_VALUE =
      new ImmutableAggregationFactory<>(
          LastValueAggregation.LONG_INSTANCE, LastValueAggregation.DOUBLE_INSTANCE);

  static final AggregationFactory MIN_MAX_SUM_COUNT =
      new ImmutableAggregationFactory<>(
          MinMaxSumCountAggregation.LONG_INSTANCE, MinMaxSumCountAggregation.DOUBLE_INSTANCE);

  private final Aggregation<L> longAggregation;
  private final Aggregation<D> doubleAggregation;

  private ImmutableAggregationFactory(
      Aggregation<L> longAggregation, Aggregation<D> doubleAggregation) {
    this.longAggregation = longAggregation;
    this.doubleAggregation = doubleAggregation;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Accumulation> Aggregation<V> create(InstrumentValueType instrumentValueType) {
    switch (instrumentValueType) {
      case LONG:
        return (Aggregation<V>) longAggregation;
      case DOUBLE:
        return (Aggregation<V>) doubleAggregation;
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
