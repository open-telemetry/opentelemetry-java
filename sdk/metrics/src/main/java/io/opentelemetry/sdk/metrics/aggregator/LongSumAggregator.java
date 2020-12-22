/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;

public final class LongSumAggregator extends Aggregator {

  private static final AggregatorFactory AGGREGATOR_FACTORY = LongSumAggregator::new;

  private final LongAdder current = new LongAdder();

  /**
   * Returns an {@link AggregatorFactory} that produces {@link LongSumAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link LongSumAggregator} instances.
   */
  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  protected Accumulation doAccumulateThenReset() {
    return LongAccumulation.create(this.current.sumThenReset());
  }

  @Override
  public void doRecordLong(long value) {
    current.add(value);
  }
}
