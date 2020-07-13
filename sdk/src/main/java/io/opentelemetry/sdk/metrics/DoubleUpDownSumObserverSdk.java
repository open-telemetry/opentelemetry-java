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
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractDoubleAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleUpDownSumObserverSdk extends AbstractDoubleAsynchronousInstrument
    implements DoubleUpDownSumObserver {

  DoubleUpDownSumObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleUpDownSumObserverSdk.Builder>
      implements DoubleUpDownSumObserver.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        MeterSdk meterSdk) {
      super(name, meterProviderSharedState, meterSharedState, meterSdk);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleUpDownSumObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.DOUBLE);
      return register(
          new DoubleUpDownSumObserverSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor)));
    }
  }
}
