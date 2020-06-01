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

import io.opentelemetry.metrics.LongUpDownSumObserver;
import io.opentelemetry.metrics.LongUpDownSumObserver.ResultLongUpDownSumObserver;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class LongUpDownSumObserverSdk
    extends AbstractAsynchronousInstrument<ResultLongUpDownSumObserver>
    implements LongUpDownSumObserver {
  LongUpDownSumObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(
            Batchers.getCumulativeAllLabels(
                descriptor, meterProviderSharedState, meterSharedState, Aggregations.lastValue())));
  }

  @Override
  ResultLongUpDownSumObserver newResult(ActiveBatcher activeBatcher) {
    return new ResultLongUpDownSumObserverSdk(activeBatcher);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<LongUpDownSumObserverSdk.Builder>
      implements LongUpDownSumObserver.Builder {

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
    public LongUpDownSumObserverSdk build() {
      return register(
          new LongUpDownSumObserverSdk(
              getInstrumentDescriptor(
                  InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }

  private static final class ResultLongUpDownSumObserverSdk implements ResultLongUpDownSumObserver {

    private final ActiveBatcher activeBatcher;

    private ResultLongUpDownSumObserverSdk(ActiveBatcher activeBatcher) {
      this.activeBatcher = activeBatcher;
    }

    @Override
    public void observe(long sum, String... keyValueLabelPairs) {
      Aggregator aggregator = activeBatcher.getAggregator();
      aggregator.recordLong(sum);
      activeBatcher.batch(
          LabelSetSdk.create(keyValueLabelPairs), aggregator, /* mappedAggregator= */ false);
    }
  }
}
