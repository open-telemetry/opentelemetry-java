/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
import java.util.concurrent.atomic.DoubleAdder;

public final class DoubleSumAggregator extends AbstractAggregator {

  private static final AggregatorFactory AGGREGATOR_FACTORY = DoubleSumAggregator::new;

  private final DoubleAdder current = new DoubleAdder();

  /**
   * Returns an {@link AggregatorFactory} that produces {@link DoubleSumAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link DoubleSumAggregator} instances.
   */
  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  Accumulation doToAccumulationThenReset() {
    return DoubleAccumulation.create(this.current.sumThenReset());
  }

  @Override
  public void doRecordDouble(double value) {
    current.add(value);
  }
}
