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

import io.opentelemetry.metrics.LongSumObserver;
import io.opentelemetry.metrics.LongSumObserver.ResultLongSumObserver;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class LongSumObserverSdk extends AbstractAsynchronousInstrument<ResultLongSumObserver>
    implements LongSumObserver {
  LongSumObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(
            getDefaultBatcher(
                descriptor, meterProviderSharedState, meterSharedState, Aggregations.lastValue())));
  }

  @Override
  ResultLongSumObserver newResult(ActiveBatcher activeBatcher) {
    return new ResultLongObserverSdk(activeBatcher);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<LongSumObserverSdk.Builder>
      implements LongSumObserver.Builder {

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
    public LongSumObserverSdk build() {
      return register(
          new LongSumObserverSdk(
              getInstrumentDescriptor(InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }

  private static final class ResultLongObserverSdk implements ResultLongSumObserver {

    private final ActiveBatcher activeBatcher;

    private ResultLongObserverSdk(ActiveBatcher activeBatcher) {
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
