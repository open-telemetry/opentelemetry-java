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

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;

/**
 * Tracks a list of active Batchers used to aggregate measurements recorded by one {@code
 * Instrument}.
 *
 * <p>TODO: Add support for multiple "Batchers" in the same time.
 *
 * <p>TODO: Consider if support for changing batchers at runtime is needed.
 */
final class ActiveBatcher implements Batcher {
  private final Batcher batcher;

  ActiveBatcher(Batcher batcher) {
    this.batcher = batcher;
  }

  @Override
  public Aggregator getAggregator() {
    return batcher.getAggregator();
  }

  @Override
  public void batch(LabelSetSdk labelSet, Aggregator aggregator, boolean mappedAggregator) {
    batcher.batch(labelSet, aggregator, mappedAggregator);
  }

  @Override
  public List<MetricData> completeCollectionCycle() {
    return batcher.completeCollectionCycle();
  }
}
