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

import io.opentelemetry.metrics.DoubleValueObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractDoubleAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class DoubleValueObserverSdk extends AbstractDoubleAsynchronousInstrument
    implements DoubleValueObserver {

  DoubleValueObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        // TODO: Revisit the batcher used here, currently this does not remove duplicate records in
        //  the same cycle.
        new ActiveBatcher(
            Batchers.getDeltaAllLabels(
                descriptor,
                meterProviderSharedState,
                meterSharedState,
                Aggregations.minMaxSumCount())));
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleValueObserverSdk.Builder>
      implements DoubleValueObserver.Builder {

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
    public DoubleValueObserverSdk build() {
      return register(
          new DoubleValueObserverSdk(
              getInstrumentDescriptor(InstrumentType.VALUE_OBSERVER, InstrumentValueType.DOUBLE),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }
}
