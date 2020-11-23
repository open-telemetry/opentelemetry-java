/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// todo: there is a race condition here that needs to be fixed. `getOrCreate` will be being called
// for every recording. However, when a collection happens, we need to coordinate the cleaning of
// the map (if it's requested in the `getAggregatorsForInstrument` call). Otherwise, recordings
// made on the resulting aggregator could be missed by the accumulator.
class AggregatorLookup {
  private final Map<InstrumentKey, Map<Labels, LongAggregator<?>>> data = new ConcurrentHashMap<>();

  LongAggregator<?> getOrCreate(
      InstrumentKey instrumentKey, Labels labels, AggregatorMaker creator) {
    Map<Labels, LongAggregator<?>> registeredLabels =
        data.computeIfAbsent(instrumentKey, k -> new ConcurrentHashMap<>());
    return registeredLabels.computeIfAbsent(labels, l -> creator.make());
  }

  Collection<InstrumentKey> getActiveInstrumentKeys() {
    return data.keySet();
  }

  Map<Labels, LongAggregator<?>> getAggregatorsForInstrument(
      InstrumentKey instrumentKey, boolean clean) {
    if (clean) {
      return data.put(instrumentKey, new ConcurrentHashMap<>());
    }
    return data.get(instrumentKey);
  }

  interface AggregatorMaker {
    LongAggregator<?> make();
  }
}
