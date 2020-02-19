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

import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.List;

/** A collection of available Batchers. */
final class Batchers {

  static Batcher getNoop() {
    return Noop.INSTANCE;
  }

  private static final class Noop implements Batcher {
    private static final Noop INSTANCE = new Noop();

    @Override
    public Aggregator getAggregator() {
      return NoopAggregator.getFactory().getAggregator();
    }

    @Override
    public void batch(LabelSet labelSet, Aggregator aggregator, boolean mappedAggregator) {}

    @Override
    public List<MetricData> completeCollectionCycle() {
      return Collections.emptyList();
    }
  }

  private Batchers() {}
}
