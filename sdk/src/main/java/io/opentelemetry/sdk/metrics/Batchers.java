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

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.view.Aggregation;
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

  /**
   * Create a Batcher that uses the "cumulative" Temporality and uses all labels for aggregation.
   * "Cumulative" means that all metrics that are generated will be considered for the lifetime of
   * the Instrument being aggregated.
   */
  static Batcher getCumulativeAllLabels(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregation aggregation) {
    return new AllLabels(
        getDefaultMetricDescriptor(descriptor, aggregation),
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        aggregation.getAggregatorFactory(descriptor.getValueType()),
        meterProviderSharedState.getClock(),
        /* delta= */ false);
  }

  /**
   * Create a Batcher that uses the "delta" Temporality and uses all labels for aggregation. "Delta"
   * means that all metrics that are generated are only for the most recent collection interval.
   */
  static Batcher getDeltaAllLabels(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregation aggregation) {
    return new AllLabels(
        getDefaultMetricDescriptor(descriptor, aggregation),
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        aggregation.getAggregatorFactory(descriptor.getValueType()),
        meterProviderSharedState.getClock(),
        /* delta= */ true);
  }

  private static final class Noop implements Batcher {
    private static final Noop INSTANCE = new Noop();

    @Override
    public Aggregator getAggregator() {
      return NoopAggregator.getFactory().getAggregator();
    }

    @Override
    public void batch(Labels labelSet, Aggregator aggregator, boolean mappedAggregator) {}

    @Override
    public List<MetricData> completeCollectionCycle() {
      return Collections.emptyList();
    }

    @Override
    public boolean generatesDeltas() {
      return false;
    }
  }

  private static final class AllLabels implements Batcher {
    private final Descriptor descriptor;
    private final Resource resource;
    private final InstrumentationLibraryInfo instrumentationLibraryInfo;
    private final Clock clock;
    private final AggregatorFactory aggregatorFactory;
    private Map<Labels, Aggregator> aggregatorMap;
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
    public final void batch(Labels labelSet, Aggregator aggregator, boolean unmappedAggregator) {
      Aggregator currentAggregator = aggregatorMap.get(labelSet);
      if (currentAggregator == null) {
        // This aggregator is not mapped, we can use this instance.
        if (unmappedAggregator) {
          aggregatorMap.put(labelSet, aggregator);
          return;
        }
        currentAggregator = aggregatorFactory.getAggregator();
        aggregatorMap.put(labelSet, currentAggregator);
      }
      aggregator.mergeToAndReset(currentAggregator);
    }

    @Override
    public final List<MetricData> completeCollectionCycle() {
      List<Point> points = new ArrayList<>(aggregatorMap.size());
      long epochNanos = clock.now();
      for (Map.Entry<Labels, Aggregator> entry : aggregatorMap.entrySet()) {
        Point point = entry.getValue().toPoint(startEpochNanos, epochNanos, entry.getKey());
        if (point != null) {
          points.add(point);
        }
      }
      if (delta) {
        startEpochNanos = epochNanos;
        aggregatorMap = new HashMap<>();
      }
      return Collections.singletonList(
          MetricData.create(descriptor, resource, instrumentationLibraryInfo, points));
    }

    @Override
    public boolean generatesDeltas() {
      return delta;
    }
  }

  private static Descriptor getDefaultMetricDescriptor(
      InstrumentDescriptor descriptor, Aggregation aggregation) {
    return Descriptor.create(
        descriptor.getName(),
        descriptor.getDescription(),
        aggregation.getUnit(descriptor.getUnit()),
        aggregation.getDescriptorType(descriptor.getType(), descriptor.getValueType()),
        descriptor.getConstantLabels());
  }

  private Batchers() {}
}
