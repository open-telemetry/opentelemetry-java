/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 */
public final class LongLastValueAggregator extends AbstractAggregator {

  @Nullable private static final Long DEFAULT_VALUE = null;
  private static final AggregatorFactory AGGREGATOR_FACTORY =
      new AggregatorFactory() {
        @Override
        public Aggregator getAggregator() {
          return new LongLastValueAggregator();
        }
      };

  private final AtomicReference<Long> current = new AtomicReference<>(DEFAULT_VALUE);

  /**
   * Returns an {@link AggregatorFactory} that produces {@link LongLastValueAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link LongLastValueAggregator} instances.
   */
  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  void doMergeAndReset(Aggregator aggregator) {
    LongLastValueAggregator other = (LongLastValueAggregator) aggregator;
    other.current.set(this.current.getAndSet(DEFAULT_VALUE));
  }

  @Override
  @Nullable
  public Point toPoint(long startEpochNanos, long epochNanos, Map<String, String> labels) {
    @Nullable Long currentValue = current.get();
    return currentValue == null
        ? null
        : LongPoint.create(startEpochNanos, epochNanos, labels, current.get());
  }

  @Override
  public void recordLong(long value) {
    current.set(value);
  }
}
