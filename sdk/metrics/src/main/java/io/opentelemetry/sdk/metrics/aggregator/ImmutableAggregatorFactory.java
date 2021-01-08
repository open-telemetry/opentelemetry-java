/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.accumulation.Accumulation;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ImmutableAggregatorFactory<L extends Accumulation, D extends Accumulation>
    implements AggregatorFactory {
  static final AggregatorFactory SUM =
      new ImmutableAggregatorFactory<>(
          LongSumAggregator.getInstance(), DoubleSumAggregator.getInstance());

  static final AggregatorFactory COUNT =
      new ImmutableAggregatorFactory<>(
          CountAggregator.getInstance(), CountAggregator.getInstance());

  static final AggregatorFactory LAST_VALUE =
      new ImmutableAggregatorFactory<>(
          LongLastValueAggregator.getInstance(), DoubleLastValueAggregator.getInstance());

  static final AggregatorFactory MIN_MAX_SUM_COUNT =
      new ImmutableAggregatorFactory<>(
          LongMinMaxSumCountAggregator.getInstance(), DoubleMinMaxSumCountAggregator.getInstance());

  private final Aggregator<L> longAggregator;
  private final Aggregator<D> doubleAggregator;

  private ImmutableAggregatorFactory(Aggregator<L> longAggregator, Aggregator<D> doubleAggregator) {
    this.longAggregator = longAggregator;
    this.doubleAggregator = doubleAggregator;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Accumulation> Aggregator<T> create(InstrumentValueType instrumentValueType) {
    switch (instrumentValueType) {
      case LONG:
        return (Aggregator<T>) longAggregator;
      case DOUBLE:
        return (Aggregator<T>) doubleAggregator;
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }
}
