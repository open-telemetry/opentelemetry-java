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

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

abstract class AbstractInstrumentWithBinding<B extends AbstractBoundInstrument>
    extends AbstractInstrument {
  private final ConcurrentHashMap<LabelSetSdk, B> boundLabels;
  private final ReentrantLock collectLock;

  AbstractInstrumentWithBinding(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      ActiveBatcher activeBatcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
    boundLabels = new ConcurrentHashMap<>();
    collectLock = new ReentrantLock();
  }

  final B bind(LabelSetSdk labelSet) {
    B binding = boundLabels.get(labelSet);
    if (binding != null && binding.bind()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return binding;
    }

    // Missing entry or no longer mapped, try to add a new entry.
    binding = newBinding(getActiveBatcher());
    while (true) {
      B oldBound = boundLabels.putIfAbsent(labelSet, binding);
      if (oldBound != null) {
        if (oldBound.bind()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return oldBound;
        }
        // Try to remove the oldBound. This will race with the collect method, but only one will
        // succeed.
        boundLabels.remove(labelSet, oldBound);
        continue;
      }
      return binding;
    }
  }

  /**
   * Collects records from all the entries (labelSet, Bound) that changed since the last collect()
   * call.
   */
  @Override
  final List<MetricData> collectAll() {
    collectLock.lock();
    try {
      Batcher batcher = getActiveBatcher();
      for (Map.Entry<LabelSetSdk, B> entry : boundLabels.entrySet()) {
        boolean unmappedEntry = entry.getValue().tryUnmap();
        if (unmappedEntry) {
          // If able to unmap then remove the record from the current Map. This can race with the
          // acquire but because we requested a specific value only one will succeed.
          boundLabels.remove(entry.getKey(), entry.getValue());
        }
        batcher.batch(entry.getKey(), entry.getValue().getAggregator(), unmappedEntry);
      }
      return batcher.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  abstract B newBinding(Batcher batcher);
}
