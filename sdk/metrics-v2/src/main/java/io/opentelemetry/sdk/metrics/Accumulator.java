/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Accumulator {
  private final Processor processor;

  private final ConcurrentMap<AggregatorKey, LongAggregator> longAggregators =
      new ConcurrentHashMap<>();

  Accumulator(Processor processor) {
    this.processor = processor;
  }

  void recordLongAdd(
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      Labels labels,
      long increment) {
    // find/create the appropriate aggregator "record" based on iLI, ID and Labels

    // todo: avoid allocating a key with every recording. maybe nested maps? maybe the equivalent
    //  of an AllLabels implementation from v1 metrics?
    LongAggregator longAggregator =
        longAggregators.computeIfAbsent(
            AggregatorKey.create(instrumentationLibraryInfo, instrumentDescriptor, labels),
            Accumulator::lookupLongAggregator);
    longAggregator.record(increment);
  }

  private static LongAggregator lookupLongAggregator(AggregatorKey aggregatorKey) {
    // todo: look up from a ViewRegistry-like thingee.
    return new LongSumAggregator();
  }

  // called by the Controller, either on an interval, or when a pull-based exporter needs it.
  void collect() {
    // this method will take all the accumulations and pass them on to the Processor
    // it will do this for each of the Aggregators that have been created this collection cycle,
    // doing a snapshot ("synchronized move") on each of them to get the appropriate Accumulation
    // data. note: I assume that the snapshotting will be something that is done internal to the
    // aggregator implementations, but I could see it going either way.
    // todo: finish implementing me

    // things to do:
    // - if an aggregator is unused, in a cycle, deal with that case. does the aggregator signal
    // that
    // to this with a method call? like aggregator.hasRecordings() ?
    // - when to remove the keys from the map? maybe if it has no recording in the interval?
    // - double typed aggregators
    // -
    for (Map.Entry<AggregatorKey, LongAggregator> entry : longAggregators.entrySet()) {
      AggregatorKey key = entry.getKey();
      LongAggregator longAggregator = entry.getValue();
      processor.process(key, longAggregator.collect());
    }
  }
}
