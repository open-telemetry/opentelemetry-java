/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
import java.util.concurrent.atomic.DoubleAdder;

public final class DoubleSumAggregator extends Aggregator<DoubleAccumulation> {
  private static final AggregatorFactory<DoubleAccumulation> AGGREGATOR_FACTORY =
      new AggregatorFactory<DoubleAccumulation>() {
        @Override
        public Aggregator<DoubleAccumulation> getAggregator() {
          return new DoubleSumAggregator();
        }

        @Override
        public DoubleAccumulation accumulateDouble(double value) {
          return DoubleAccumulation.create(value);
        }
      };

  private final DoubleAdder current = new DoubleAdder();

  private DoubleSumAggregator() {}

  /**
   * Returns an {@link AggregatorFactory} that produces {@link DoubleSumAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link DoubleSumAggregator} instances.
   */
  public static AggregatorFactory<DoubleAccumulation> getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  protected DoubleAccumulation doAccumulateThenReset() {
    return DoubleAccumulation.create(this.current.sumThenReset());
  }

  @Override
  protected void doRecordDouble(double value) {
    current.add(value);
  }
}
