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

import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

final class LongObserverSdk extends AbstractObserver implements LongObserver {
  @Nullable private volatile Callback<ResultLongObserver> metricUpdater = null;
  private final ReentrantLock collectLock = new ReentrantLock();

  LongObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      boolean monotonic) {
    super(
        descriptor,
        InstrumentValueType.LONG,
        meterProviderSharedState,
        meterSharedState,
        monotonic);
  }

  @Override
  List<MetricData> collectAll() {
    Callback<ResultLongObserver> currentMetricUpdater = metricUpdater;
    if (currentMetricUpdater == null) {
      return Collections.emptyList();
    }
    collectLock.lock();
    try {
      final ActiveBatcher activeBatcher = getActiveBatcher();
      currentMetricUpdater.update(new ResultLongObserverSdk(activeBatcher, isMonotonic()));
      return activeBatcher.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  @Override
  public void setCallback(Callback<ResultLongObserver> metricUpdater) {
    this.metricUpdater = Objects.requireNonNull(metricUpdater, "metricUpdater");
  }

  static final class Builder extends AbstractObserver.Builder<LongObserverSdk.Builder>
      implements LongObserver.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongObserverSdk build() {
      return register(
          new LongObserverSdk(
              getInstrumentDescriptor(),
              getMeterProviderSharedState(),
              getMeterSharedState(),
              isMonotonic()));
    }
  }

  private static final class ResultLongObserverSdk implements ResultLongObserver {

    private final ActiveBatcher activeBatcher;
    private final boolean monotonic;

    private ResultLongObserverSdk(ActiveBatcher activeBatcher, boolean monotonic) {
      this.activeBatcher = activeBatcher;
      this.monotonic = monotonic;
    }

    @Override
    public void observe(long value, String... keyValueLabelPairs) {
      if (monotonic && value < 0) {
        throw new IllegalArgumentException("monotonic observers can only record positive values");
      }
      Aggregator aggregator = activeBatcher.getAggregator();
      aggregator.recordLong(value);
      activeBatcher.batch(
          LabelSetSdk.create(keyValueLabelPairs), aggregator, /* mappedAggregator= */ false);
    }
  }
}
