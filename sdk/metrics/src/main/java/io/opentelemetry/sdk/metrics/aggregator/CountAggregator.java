/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;

public final class CountAggregator extends AbstractAggregator {
  private static final AggregatorFactory AGGREGATOR_FACTORY = CountAggregator::new;

  private final LongAdder current;

  public CountAggregator() {
    this.current = new LongAdder();
  }

  /**
   * Returns an {@link AggregatorFactory} that produces {@link CountAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link CountAggregator} instances.
   */
  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  public void doRecordLong(long value) {
    current.add(1);
  }

  @Override
  public void doRecordDouble(double value) {
    current.add(1);
  }

  @Override
  Accumulation doToAccumulationThenReset() {
    return LongAccumulation.create(current.sumThenReset());
  }
}
