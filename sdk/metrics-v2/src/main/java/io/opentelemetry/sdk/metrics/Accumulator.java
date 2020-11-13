/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("rawtypes")
class Accumulator {
  private final Clock clock;
  private final AggregatorLookup lookup = new AggregatorLookup();

  Accumulator(Clock clock) {
    this.clock = clock;
  }

  void recordLongAdd(InstrumentKey instrumentKey, Labels labels, long increment) {
    LongAggregator<?> longAggregator =
        lookup.getOrCreate(instrumentKey, labels, () -> lookupLongAggregator(instrumentKey));

    longAggregator.record(increment);
  }

  @SuppressWarnings("unused")
  private LongAggregator lookupLongAggregator(InstrumentKey instrumentKey) {
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

    Collection<InstrumentKey> instrumentKeys = lookup.getActiveInstrumentKeys();
    for (InstrumentKey instrumentKey : instrumentKeys) {
      Map<Labels, LongAggregator<?>> aggregators =
          lookup.getAggregatorsForInstrument(instrumentKey, /* clean=*/ true);
      // todo: change the processor to also talk InstrumentKeys.
      for (Map.Entry<Labels, LongAggregator<?>> entry : aggregators.entrySet()) {
        processor.process(
            instrumentKey,
            AggregatorKey.create(
                instrumentKey.libraryInfo(), instrumentKey.instrumentDescriptor(), entry.getKey()),
            entry.getValue().collect(clock));
      }
    }
  }
}
