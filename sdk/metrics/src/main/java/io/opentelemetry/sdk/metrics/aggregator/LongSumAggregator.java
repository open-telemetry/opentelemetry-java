/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.concurrent.atomic.LongAdder;

public final class LongSumAggregator extends AbstractAggregator {

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
  void doMergeAndReset(Aggregator aggregator) {
    LongSumAggregator other = (LongSumAggregator) aggregator;
    other.current.add(this.current.sumThenReset());
  }

  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return LongPoint.create(startEpochNanos, epochNanos, labels, current.sum());
  }

  @Override
  public void doRecordLong(long value) {
    current.add(value);
  }
}
