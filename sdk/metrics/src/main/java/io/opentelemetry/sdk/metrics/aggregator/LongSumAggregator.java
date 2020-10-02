/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.concurrent.atomic.AtomicLong;

public final class LongSumAggregator extends AbstractAggregator {

  private static final long DEFAULT_VALUE = 0L;
  private static final AggregatorFactory AGGREGATOR_FACTORY =
      new AggregatorFactory() {
        @Override
        public Aggregator getAggregator() {
          return new LongSumAggregator();
        }
      };

  // TODO: Change to use LongAdder when changed to java8.
  private final AtomicLong current = new AtomicLong(DEFAULT_VALUE);

  /**
   * Returns an {@link AggregatorFactory} that produces {@link LongSumAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link LongSumAggregator} instances.
   */
  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  void doMergeAndReset(Aggregator aggregator) {
    LongSumAggregator other = (LongSumAggregator) aggregator;
    other.current.getAndAdd(this.current.getAndSet(DEFAULT_VALUE));
  }

  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return LongPoint.create(startEpochNanos, epochNanos, labels, current.get());
  }

  @Override
  public void doRecordLong(long value) {
    current.getAndAdd(value);
  }
}
