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

import io.opentelemetry.metrics.DoubleUpDownSumObserver;
import io.opentelemetry.metrics.DoubleUpDownSumObserver.ResultDoubleUpDownSumObserver;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class DoubleUpDownSumObserverSdk
    extends AbstractAsynchronousInstrument<ResultDoubleUpDownSumObserver>
    implements DoubleUpDownSumObserver {

  DoubleUpDownSumObserverSdk(
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
  ResultDoubleUpDownSumObserver newResult(ActiveBatcher activeBatcher) {
    return new ResultDoubleUpDownSumObserverSdk(activeBatcher);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleUpDownSumObserverSdk.Builder>
      implements DoubleUpDownSumObserver.Builder {

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
    public DoubleUpDownSumObserverSdk build() {
      return register(
          new DoubleUpDownSumObserverSdk(
              getInstrumentDescriptor(
                  InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.DOUBLE),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }

  private static final class ResultDoubleUpDownSumObserverSdk
      implements ResultDoubleUpDownSumObserver {

    private final ActiveBatcher activeBatcher;

    private ResultDoubleUpDownSumObserverSdk(ActiveBatcher activeBatcher) {
      this.activeBatcher = activeBatcher;
    }

    @Override
    public void observe(double sum, String... keyValueLabelPairs) {
      Aggregator aggregator = activeBatcher.getAggregator();
      aggregator.recordDouble(sum);
      activeBatcher.batch(
          LabelSetSdk.create(keyValueLabelPairs), aggregator, /* mappedAggregator= */ false);
    }
  }
}
