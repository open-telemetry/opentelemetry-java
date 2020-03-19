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

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A collection of available Batchers. */
final class Batchers {

  static Batcher getNoop() {
    return Noop.INSTANCE;
  }

  static Batcher getCumulativeAllLabels(
      Descriptor descriptor,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      AggregatorFactory aggregatorFactory,
      Clock clock) {
    return new AllLabels(
        descriptor,
        resource,
        instrumentationLibraryInfo,
        aggregatorFactory,
        clock,
        /* delta= */ false);
  }

  private static final class Noop implements Batcher {
    private static final Noop INSTANCE = new Noop();

    @Override
    public Aggregator getAggregator() {
      return NoopAggregator.getFactory().getAggregator();
    }

    @Override
    public void batch(LabelSetSdk labelSet, Aggregator aggregator, boolean mappedAggregator) {}

    @Override
    public List<MetricData> completeCollectionCycle() {
      return Collections.emptyList();
    }
  }

  private static final class AllLabels implements Batcher {
    private final Descriptor descriptor;
    private final Resource resource;
    private final InstrumentationLibraryInfo instrumentationLibraryInfo;
    private final Clock clock;
    private final AggregatorFactory aggregatorFactory;
    private Map<Map<String, String>, Aggregator> aggregatorMap;
    private long startEpochNanos;
    private final boolean delta;

    private AllLabels(
        Descriptor descriptor,
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        AggregatorFactory aggregatorFactory,
        Clock clock,
        boolean delta) {
      this.descriptor = descriptor;
      this.resource = resource;
      this.instrumentationLibraryInfo = instrumentationLibraryInfo;
      this.clock = clock;
      this.aggregatorFactory = aggregatorFactory;
      this.delta = delta;
      this.aggregatorMap = new HashMap<>();
      startEpochNanos = clock.now();
    }

    @Override
    public final Aggregator getAggregator() {
      return aggregatorFactory.getAggregator();
    }

    @Override
    public final void batch(
        LabelSetSdk labelSet, Aggregator aggregator, boolean unmappedAggregator) {
      Map<String, String> labels = labelSet.getLabels();
      Aggregator currentAggregator = aggregatorMap.get(labels);
      if (currentAggregator == null) {
        // This aggregator is not mapped, we can use this instance.
        if (unmappedAggregator) {
          aggregatorMap.put(labels, aggregator);
          return;
        }
        currentAggregator = aggregatorFactory.getAggregator();
        aggregatorMap.put(labels, currentAggregator);
      }
      aggregator.mergeToAndReset(currentAggregator);
    }

    @Override
    public final List<MetricData> completeCollectionCycle() {
      List<Point> points = new ArrayList<>(aggregatorMap.size());
      long epochNanos = clock.now();
      for (Map.Entry<Map<String, String>, Aggregator> entry : aggregatorMap.entrySet()) {
        points.add(entry.getValue().toPoint(startEpochNanos, epochNanos, entry.getKey()));
      }
      if (delta) {
        startEpochNanos = epochNanos;
        aggregatorMap = new HashMap<>();
      }
      return Collections.singletonList(
          MetricData.create(descriptor, resource, instrumentationLibraryInfo, points));
    }
  }

  private Batchers() {}
}
