/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;

public final class LongSumAggregator extends Aggregator<LongAccumulation> {
  private static final AggregatorFactory<LongAccumulation> AGGREGATOR_FACTORY =
      new AggregatorFactory<LongAccumulation>() {
        @Override
        public Aggregator<LongAccumulation> getAggregator() {
          return new LongSumAggregator();
        }

        @Override
        public LongAccumulation accumulateLong(long value) {
          return LongAccumulation.create(value);
        }
      };

  private final LongAdder current = new LongAdder();

  private LongSumAggregator() {}

  /**
   * Returns an {@link AggregatorFactory} that produces {@link LongSumAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link LongSumAggregator} instances.
   */
  public static AggregatorFactory<LongAccumulation> getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  protected LongAccumulation doAccumulateThenReset() {
    return LongAccumulation.create(this.current.sumThenReset());
  }

  @Override
  public void doRecordLong(long value) {
    current.add(value);
  }
}
