/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// todo: this probably needs to be an interface eventually, so you can have custom processors, or
// nested processors, or other fancy features.
@SuppressWarnings("rawtypes")
class Processor {
  private final ConcurrentMap<AggregatorKey, LongAggregator> longAggregators =
      new ConcurrentHashMap<>();
  private final Resource resource;
  private final Clock clock;

  Processor(Resource resource, Clock clock) {
    this.resource = resource;
    this.clock = clock;
  }

  void start() {
    // todo: flag that we're within a collection cycle, and should be accumulating values.
    // is this actually needed... wait for processor specs to see what is required here?
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  void process(AggregatorKey key, Accumulation accumulation) {
    // todo generics aren't working out down here...
    LongAggregator longAggregator = longAggregators.computeIfAbsent(key, this::createAggregator);
    longAggregator.merge(accumulation);
  }

  @SuppressWarnings("rawtypes")
  private LongAggregator createAggregator(AggregatorKey aggregatorKey) {
    // todo: look up aggregator creator, based on key. Here, they may be delta-only or cumulative,
    // as configured.
    return new LongSumAggregator(clock.now(), /* keepCumulativeSums= */ true);
  }

  // todo: use the standard MetricData, rather than our copy of it.
  Collection<MetricData> finish() {
    List<MetricData> exportData = new ArrayList<>();
    // todo: get all the data from the aggregators, transform it into MetricData and return it.
    longAggregators.forEach(
        (aggregatorKey, longAggregator) -> {
          // note: this `collect()` call resets the aggregator, if it's doing deltas
          Accumulation accumulation = longAggregator.collect(clock);
          // todo: group by labels so we don't repeat ourselves so badly; this probably
          // needs to be done up at a higher level. If we have a better data structure for
          // storing label-set aggregators per aggregation key, this would be solved, I
          // think.
          MetricData.Point point =
              accumulation.convertToPoint(clock.now(), aggregatorKey.getLabels());
          InstrumentDescriptor instrumentDescriptor = aggregatorKey.getInstrumentDescriptor();
          exportData.add(
              MetricData.create(
                  resource,
                  aggregatorKey.getInstrumentationLibraryInfo(),
                  instrumentDescriptor.getName(),
                  instrumentDescriptor.getDescription(),
                  instrumentDescriptor.getUnit(),
                  accumulation.getMetricDataType(),
                  Collections.singleton(point)));
        });

    return exportData;
  }
}
