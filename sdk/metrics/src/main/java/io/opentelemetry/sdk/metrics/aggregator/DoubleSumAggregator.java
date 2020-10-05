/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;

public final class DoubleSumAggregator extends AbstractAggregator {

  private static final double DEFAULT_VALUE = 0.0;
  private static final AggregatorFactory AGGREGATOR_FACTORY =
      new AggregatorFactory() {
        @Override
        public Aggregator getAggregator() {
          return new DoubleSumAggregator();
        }
      };

  // TODO: Change to use DoubleAdder when changed to java8.
  private final AtomicDouble current = new AtomicDouble(DEFAULT_VALUE);

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
    other.current.getAndAdd(this.current.getAndSet(DEFAULT_VALUE));
  }

  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return DoublePoint.create(startEpochNanos, epochNanos, labels, current.get());
  }

  @Override
  public void doRecordDouble(double value) {
    current.getAndAdd(value);
  }
}
