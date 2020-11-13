/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// todo: this probably needs to be an interface eventually, so you can have custom processors, or
//  nested processors, or other fancy features.
class Processor {
  private final AggregatorLookup aggregatorLookup = new AggregatorLookup();

  private final Resource resource;
  private final Clock clock;

  Processor(Resource resource, Clock clock) {
    this.resource = resource;
    this.clock = clock;
  }

  void start() {
    // todo: flag that we're within a collection cycle, and should be accumulating values.
    //  is this actually needed... wait for processor specs to see what is required here?
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  void process(InstrumentKey instrumentKey, AggregatorKey key, Accumulation accumulation) {
    LongAggregator longAggregator =
        aggregatorLookup.getOrCreate(
            instrumentKey, key.getLabels(), () -> createAggregator(instrumentKey));
    longAggregator.merge(accumulation);
  }

  @SuppressWarnings({"rawtypes", "unused"})
  private LongAggregator createAggregator(InstrumentKey key) {
    // todo: look up aggregator creator, based on key. Here, they may be delta-only or cumulative,
    //  as configured.
    return new LongSumAggregator(clock.now(), /* keepCumulativeSums= */ true);
  }

  // todo: use the standard MetricData, rather than our copy of it.
  Collection<MetricData> finish() {
    List<MetricData> exportData = new ArrayList<>();
    // todo: get all the data from the aggregators, transform it into MetricData and return it.
    Collection<InstrumentKey> instrumentKeys = aggregatorLookup.getActiveInstrumentKeys();
    for (InstrumentKey key : instrumentKeys) {
      Map<Labels, LongAggregator<?>> aggregators =
          aggregatorLookup.getAggregatorsForInstrument(key, /* clean=*/ false);
      List<MetricData.Point> points = new ArrayList<>();

      MetricData.Type metricDataType = null;
      for (Map.Entry<Labels, LongAggregator<?>> entry : aggregators.entrySet()) {
        Accumulation accumulation = entry.getValue().collect(clock);
        MetricData.Point point = accumulation.convertToPoint(clock.now(), entry.getKey());
        points.add(point);
        // todo: this is weird. we should only have one type per InstrumentKey. Figure out how to
        // make that work right. Probably pull the type out of the accumulation and enable looking
        // it up? It probably should be set via the ViewRegistry in some way or another. A given
        // InstrumentDescriptor should map to a single type.
        metricDataType = accumulation.getMetricDataType();
      }

      if (metricDataType == null) {
        continue;
      }
      InstrumentDescriptor instrumentDescriptor = key.instrumentDescriptor();

      exportData.add(
          MetricData.create(
              resource,
              key.libraryInfo(),
              instrumentDescriptor.getName(),
              instrumentDescriptor.getDescription(),
              instrumentDescriptor.getUnit(),
              metricDataType,
              points));
    }
    return exportData;
  }
}
