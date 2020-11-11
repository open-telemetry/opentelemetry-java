/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("rawtypes")
class Accumulator {
  private final Clock clock;
  private final ConcurrentMap<AggregatorKey, LongAggregator> longAggregators =
      new ConcurrentHashMap<>();

  Accumulator(Clock clock) {
    this.clock = clock;
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
            aggregatorKey -> lookupLongAggregator(aggregatorKey, clock));
    longAggregator.record(increment);
  }

  @SuppressWarnings("unused")
  private static LongAggregator lookupLongAggregator(AggregatorKey aggregatorKey, Clock clock) {
    // todo: look up from a ViewRegistry-like thingee. Note: this lookup needs to be identical
    // to the one done in the Processor. The difference is that in here, all the aggregators *must*
    // be configured as delta-aggregators (or we need to remove them from the map and re-create
    // them at every collection cycle...or maybe manually reset them from the code in here?),
    // and the ones in the processor are view-like and configurable, depending on your exporter
    // needs.
    return new LongSumAggregator(/* startTime=*/ clock.now(), /* keepCumulativeSums=*/ false);
  }

  // called by the Controller, either on an interval, or when a pull-based exporter needs it.
  void collectAndSendTo(Processor processor) {
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
    // - maybe, since the Accumulator can't be long-term stateful, we can just remove them all,
    // always?
    // - double typed aggregators

    for (Map.Entry<AggregatorKey, LongAggregator> entry : longAggregators.entrySet()) {
      AggregatorKey key = entry.getKey();
      LongAggregator longAggregator = entry.getValue();
      processor.process(key, longAggregator.collect(clock));
    }
  }
}
