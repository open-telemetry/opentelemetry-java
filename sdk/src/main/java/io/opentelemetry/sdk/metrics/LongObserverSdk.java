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

import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

final class LongObserverSdk extends AbstractObserver implements LongObserver {
  @Nullable private volatile Callback<ResultLongObserver> metricUpdater = null;

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
    final ActiveBatcher activeBatcher = getActiveBatcher();
    currentMetricUpdater.update(
        new ResultLongObserver() {
          @Override
          public void observe(long value, LabelSet labelSet) {
            if (isMonotonic() && value < 0) {
              throw new IllegalArgumentException(
                  "monotonic observers can only record positive values");
            }
            Aggregator aggregator = activeBatcher.getAggregator();
            aggregator.recordLong(value);
            activeBatcher.batch(labelSet, aggregator, /* mappedAggregator= */ false);
          }
        });
    return activeBatcher.completeCollectionCycle();
  }

  @Override
  public void setCallback(Callback<ResultLongObserver> metricUpdater) {
    this.metricUpdater = Utils.checkNotNull(metricUpdater, "metricUpdater");
  }

  static final class Builder extends AbstractObserver.Builder<LongObserver.Builder, LongObserver>
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
}
