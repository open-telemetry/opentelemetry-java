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

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Map;

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
  public Point toPoint(long startEpochNanos, long epochNanos, Map<String, String> labels) {
    return DoublePoint.create(startEpochNanos, epochNanos, labels, current.get());
  }

  @Override
  public void recordDouble(double value) {
    current.getAndAdd(value);
  }
}
