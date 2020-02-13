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

import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Map;
import javax.annotation.Nullable;

public final class NoopAggregator implements Aggregator {
  private static final Aggregator NOOP_AGGREGATOR = new NoopAggregator();
  private static final AggregatorFactory AGGREGATOR_FACTORY =
      new AggregatorFactory() {
        @Override
        public Aggregator getAggregator() {
          return NOOP_AGGREGATOR;
        }
      };

  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  public void mergeToAndReset(Aggregator aggregator) {
    // Noop
  }

  @Nullable
  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Map<String, String> labels) {
    return null;
  }

  @Override
  public void recordLong(long value) {
    // Noop
  }

  @Override
  public void recordDouble(double value) {
    // Noop
  }

  private NoopAggregator() {}
}
