/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
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
  void doMergeAndReset(Aggregator aggregator) {
    DoubleSumAggregator other = (DoubleSumAggregator) aggregator;
    other.current.add(this.current.sumThenReset());
  }

  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return DoublePoint.create(startEpochNanos, epochNanos, labels, current.sum());
  }

  @Override
  public void doRecordDouble(double value) {
    current.add(value);
  }
}
