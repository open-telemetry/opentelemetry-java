/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;

public final class CountAggregator extends Aggregator<LongAccumulation> {
  private static final AggregatorFactory<LongAccumulation> AGGREGATOR_FACTORY =
      new AggregatorFactory<LongAccumulation>() {
        @Override
        public Aggregator<LongAccumulation> getAggregator() {
          return new CountAggregator();
        }

        @Override
        public LongAccumulation accumulateDouble(double value) {
          return LongAccumulation.create(1);
        }

        @Override
        public LongAccumulation accumulateLong(long value) {
          return LongAccumulation.create(1);
        }
      };

  private final LongAdder current;

  private CountAggregator() {
    this.current = new LongAdder();
  }

  /**
   * Returns an {@link AggregatorFactory} that produces {@link CountAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link CountAggregator} instances.
   */
  public static AggregatorFactory<LongAccumulation> getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  protected void doRecordLong(long value) {
    current.add(1);
  }

  @Override
  protected void doRecordDouble(double value) {
    current.add(1);
  }

  @Override
  protected LongAccumulation doAccumulateThenReset() {
    return LongAccumulation.create(current.sumThenReset());
  }
}
